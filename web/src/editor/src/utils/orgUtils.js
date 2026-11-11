import _ from 'underscore'
import $ from 'jquery'
import { autocomplete } from './autocomplete'

/*
 * Looks organisations up in ROR, for contacts, funders and a service agreement's authors and rights
 * holders. Names alone can't tell same-named organisations apart (there are two "York University"s,
 * in Nebraska and Toronto), so each match says where it is, what kind it is and its website, as ROR
 * advises for its forms, and the organisation chosen says where it is beneath the field (dri-one #417).
 * Everything shown is ROR's, so it is written as text, never as markup.
 */

// The organisations seen, by ROR identifier, so the one chosen needn't be looked up again to describe it
const known = new Map()

const element = (tag, className, text) => {
  const el = document.createElement(tag)
  el.className = className
  el.textContent = text
  return el
}

/**
 * "York, Nebraska, United States". A name is said once where ROR repeats it ("Oslo, Oslo, Norway"
 * for 27,000 organisations), and a region that is a GeoNames code, as Singapore's "SG.01" is, left out.
 */
const place = org => [org.city, /^[A-Z]{2}\.\d+$/.test(org.region) ? null : org.region, org.country]
  .filter(Boolean)
  .filter((part, i, parts) => part !== parts[i - 1])
  .join(', ')

const capitalise = word => word.charAt(0).toUpperCase() + word.slice(1)

/** A website's host, as people know it: york.ac.uk, not https://www.york.ac.uk/ */
const host = website => {
  try {
    return new URL(website).hostname.replace(/^www\./, '')
  } catch {
    return ''
  }
}

/** Where an organisation is, what kind it is and its website: "York, England, United Kingdom · Education, Funder · york.ac.uk" */
export function orgDetails (org) {
  return [place(org), (org.types || []).map(capitalise).join(', '), host(org.website)]
    .filter(Boolean)
    .join(' · ')
}

/** Its name, and its acronym where it has one the name doesn't already say: "New York University (NYU)" */
export function orgName (org) {
  const acronym = (org.acronyms || [])[0]
  return acronym && !org.name.includes(acronym) ? `${org.name} (${acronym})` : org.name
}

const fold = text => text.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase()

/**
 * The alias a search matched, when it matched one better than the name or acronym, so the editor can
 * see why the organisation is offered: "York College" finds York University, Nebraska, by its alias.
 */
export function matchedAlias (org, term) {
  const words = fold(term).split(/\s+/).filter(Boolean)
  if (!words.length) return null
  const score = text => words.filter(word => fold(text).includes(word)).length
  const byName = Math.max(score(org.name), ...(org.acronyms || []).map(score))
  let best = null
  let bestScore = byName
  for (const alias of org.aliases || []) {
    if (score(alias) > bestScore) {
      best = alias
      bestScore = score(alias)
    }
  }
  return best
}

/** A match in the list: its name, and beneath it, quieter, where it is, what it is and what else it's called */
export function orgOption (org, term = '') {
  const option = element('div', 'org-option', '')
  option.append(element('span', 'org-name', orgName(org)))
  const details = orgDetails(org)
  if (details) option.append(element('span', 'org-details', details))
  const alias = matchedAlias(org, term)
  if (alias) option.append(element('span', 'org-details org-alias', `also known as ${alias}`))
  return option
}

export function createOrgAutocomplete (id, obj, identifierFieldName = 'organisationIdentifier', nameFieldName = 'organisationName') {
  const selector = `[data-name=${identifierFieldName}]`

  obj.$(id).each((i, input) => {
    autocomplete(input, {
      minLength: 2,
      source: async term => {
        term = term.trim()
        const query = _.isEmpty(term) ? '/organisation/names' : `/organisation/names?${$.param({ query: term })}`
        try {
          const data = await $.getJSON(query)
          return _.map(data, d => {
            known.set(d.id, d)
            return { label: d.name, url: d.id, org: d, term }
          })
        } catch {
          return []
        }
      },
      render: item => orgOption(item.org, item.term),
      menuClass: 'org-autocomplete',
      select: item => {
        obj.model.set(nameFieldName, item.label)
        obj.model.set(identifierFieldName, item.url)

        const $identifierField = obj.$(selector)

        if ($identifierField.length) {
          $identifierField.val(item.url)
        }
      }
    })

    // Where the organisation chosen is, so one coming back to the record can see which York it is
    // without following its ROR link
    const location = element('div', 'org-location form-text', '')
    location.id = `${input.id || _.uniqueId('org')}-location`
    location.hidden = true
    input.after(location)
    input.setAttribute('aria-describedby', [input.getAttribute('aria-describedby'), location.id].filter(Boolean).join(' '))

    const show = org => {
      const details = org ? orgDetails(org) : ''
      location.textContent = details
      location.hidden = !details
    }

    // However the identifier changes (chosen, typed over, or put in the RoR ID field by hand), the
    // description follows it; an answer for an identifier since replaced is dropped
    const describe = async identifier => {
      if (!identifier) return show(null)
      if (known.has(identifier)) return show(known.get(identifier))
      show(null)
      try {
        const [org] = await $.getJSON(`/organisation/names?${$.param({ id: identifier })}`)
        if (org) known.set(identifier, org)
        if (obj.model.get(identifierFieldName) === identifier) show(org)
      } catch {
        // Not described, rather than wrongly
      }
    }
    obj.listenTo(obj.model, `change:${identifierFieldName}`, (model, identifier) => describe(identifier))
    describe(obj.model.get(identifierFieldName))
  })

  obj.$(id).on('input', () => {
    obj.model.set(identifierFieldName, '')

    const $identifierField = obj.$(selector)

    if ($identifierField.length) {
      $identifierField.val('')
    }
  })
}
