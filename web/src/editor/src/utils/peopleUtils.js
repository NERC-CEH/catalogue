import $ from 'jquery'
import { autocomplete } from './autocomplete'
import { ORCID_PREFIX, normalisePersonIdentifier, orcidId } from './orcid'

/*
 * Helps editors give people their ORCID iD (dri-one #534), within what ORCID allows: it asks that iDs
 * not be matched to people by searching its registry by name, and that an iD a person hasn't confirmed
 * themselves be checked against the registry and against what we hold. So:
 *
 *  - "Find a person" looks up only the people the catalogue already names with an ORCID, showing their
 *    organisations and how many records name them, so namesakes can be told apart;
 *  - a person typed with no ORCID is shown anyone the catalogue holds by that name, to use only if it
 *    is the same person: a suggestion, never filled in for them;
 *  - every iD given is checked with ORCID, and the name ORCID holds for it is shown beside the one typed.
 *
 * All of it is written as text, never markup: names come from records and from ORCID.
 */

const element = (tag, className, text = '') => {
  const el = document.createElement(tag)
  el.className = className
  el.textContent = text
  return el
}

const fold = text => (text || '').normalize('NFD').replace(/\p{M}/gu, '').toLowerCase()
const words = text => fold(text).split(/[^\p{L}]+/u).filter(Boolean)

/** "Clare Rowland" */
export const personName = person => [person.givenName, person.familyName].filter(Boolean).join(' ')

/** "ORCID 0000-0002-0459-506X · UK Centre for Ecology & Hydrology, Lancaster University · in 3 records" */
export function personDetails (person) {
  const where = (person.affiliations || []).map(affiliation => affiliation.name).join(', ')
  const records = person.records === 1 ? 'in 1 record' : `in ${person.records} records`
  return [`ORCID ${person.orcid.replace(ORCID_PREFIX, '')}`, where, records].filter(Boolean).join(' · ')
}

/** A person in the look-up: their name, and beneath it, quieter, their iD, where they work and how else they're named */
export function personOption (person) {
  const option = element('div', 'person-option')
  option.append(element('span', 'person-name', personName(person)))
  option.append(element('span', 'person-details', personDetails(person)))
  if ((person.otherNames || []).length) {
    option.append(element('span', 'person-details person-alias', `also named ${person.otherNames.join(', ')}`))
  }
  return option
}

/**
 * Whether a name ORCID holds could be the one typed: the family name typed is among its words, and
 * a given name typed starts with the same letter as its first ("C." for Clare). Case and accents
 * don't count, as depositors type both freely.
 */
function sameName (orcidName, givenName, familyName) {
  const theirs = words(orcidName)
  const family = words(familyName)
  if (!theirs.length || !family.length) return false
  const given = words(givenName)
  return family.every(word => theirs.includes(word)) &&
    (!given.length || theirs[0].startsWith(given[0].charAt(0)))
}

/**
 * What to say beneath an iD, given what ORCID's registry says of it (null: it has never issued it)
 * and the name typed. A level is a Bootstrap text colour.
 */
export function orcidVerdict (record, givenName, familyName) {
  if (!record) {
    return { level: 'danger', text: 'ORCID has no record of this iD. Check it was copied correctly.' }
  }
  const name = [record.givenNames, record.familyName].filter(Boolean).join(' ')
  if (!name) {
    return { level: 'warning', text: 'This iD is in ORCID, but its holder keeps their name private: check it is this person\'s.' }
  }
  if (!familyName) {
    return { level: 'body-secondary', text: `ORCID's record for this iD: ${name}` }
  }
  const names = [name, record.creditName, ...(record.otherNames || [])].filter(Boolean)
  if (names.some(candidate => sameName(candidate, givenName, familyName))) {
    return { level: 'success', text: `✓ ORCID's record for this iD: ${name}` }
  }
  const typed = [givenName, familyName].filter(Boolean).join(' ')
  return { level: 'warning', text: `ORCID's record for this iD is ${name}, not ${typed}. Check it is the same person.` }
}

// What ORCID said of each iD asked about: a promise of its record, or of null for one never issued.
// A failure to ask isn't kept, so the next look asks again
const registry = new Map()
const askOrcid = id => {
  if (!registry.has(id)) {
    const asking = new Promise((resolve, reject) => {
      $.getJSON(`/people/orcid?${$.param({ id })}`)
        .done(resolve)
        .fail(xhr => xhr.status === 404 ? resolve(null) : reject(xhr))
    })
    asking.catch(() => registry.delete(id))
    registry.set(id, asking)
  }
  return registry.get(id)
}

// The people the catalogue names with an iD, or null when they can't be searched. Settled as the
// request does, not on jQuery's own then, which waits for a timer
const findPeople = term => new Promise(resolve => {
  $.getJSON(`/people/names?${$.param({ query: term })}`)
    .done(resolve)
    .fail(() => resolve(null))
})

/** Someone ORCID's registry names for an iD the catalogue doesn't hold yet */
export function orcidOption (orcidPerson) {
  const option = element('div', 'person-option')
  const name = [orcidPerson.givenNames, orcidPerson.familyName].filter(Boolean).join(' ')
  option.append(element('span', 'person-name', name || 'Name kept private in ORCID'))
  option.append(element('span', 'person-details', `ORCID ${orcidPerson.orcid.replace(ORCID_PREFIX, '')} · from ORCID, not yet in the catalogue`))
  return option
}

/** What the look-up offers for an iD the catalogue doesn't hold: the person ORCID names for it, or why not */
async function fromOrcid (id) {
  try {
    const orcidPerson = await askOrcid(id)
    if (!orcidPerson) return [{ notice: 'ORCID has no record of this iD. Check it was copied correctly.' }]
    return [{ label: [orcidPerson.givenNames, orcidPerson.familyName].filter(Boolean).join(' ') || orcidPerson.orcid, value: '', orcidPerson }]
  } catch {
    return [{ notice: 'ORCID couldn\'t be reached to check this iD. Paste it into the ORCID iD field below.' }]
  }
}

/** Puts a value in the model and in its field, as choosing does: the view was rendered before it */
const fill = (view, name, value) => {
  view.model.set(name, value)
  view.$(`[data-name=${name}]`).val(value)
}

/**
 * Wires a contact's person fields: the ORCID check beneath the iD, and, with lookup, the "Find a
 * person" look-up and the suggestions for a person with no iD. The view's template provides an input
 * with data-name nameIdentifier and, for the look-up, an input with the class personAutocomplete.
 */
export function createPersonLookup (view, { lookup = true } = {}) {
  const model = view.model
  const identifier = view.$('[data-name=nameIdentifier]')[0]
  if (!identifier) return

  const status = element('div', 'orcid-status form-text')
  status.id = `${identifier.id || `person-${model.cid}`}-status`
  status.hidden = true
  status.setAttribute('aria-live', 'polite')
  identifier.after(status)
  identifier.setAttribute('aria-describedby', [identifier.getAttribute('aria-describedby'), status.id].filter(Boolean).join(' '))

  const say = verdict => {
    status.className = `orcid-status form-text${verdict ? ` text-${verdict.level}` : ''}`
    status.textContent = verdict ? verdict.text : ''
    status.hidden = !verdict
  }

  // An answer for an iD since changed is dropped, as is one for a name since retyped
  const check = async () => {
    const id = orcidId(model.get('nameIdentifier'))
    if (!id) return say(null)
    if (!registry.has(id)) say({ level: 'body-secondary', text: 'Checking this iD with ORCID…' })
    try {
      const record = await askOrcid(id)
      if (orcidId(model.get('nameIdentifier')) !== id) return
      say(orcidVerdict(record, model.get('givenName'), model.get('familyName')))
    } catch {
      if (orcidId(model.get('nameIdentifier')) !== id) return
      say({ level: 'body-secondary', text: 'ORCID couldn\'t be reached to check this iD. It will be checked when the record is next opened.' })
    }
  }
  view.listenTo(model, 'change:nameIdentifier change:givenName change:familyName', check)
  check()

  if (!lookup) return

  const find = view.$('.personAutocomplete')[0]
  if (find) {
    autocomplete(find, {
      minLength: 2,
      source: async term => {
        term = term.trim()
        const id = orcidId(term)
        if (!id && normalisePersonIdentifier(term).startsWith(ORCID_PREFIX)) {
          return [{ notice: 'That isn\'t a real ORCID iD: its last character doesn\'t match the rest. Check it was copied correctly.' }]
        }
        const people = await findPeople(term)
        if (people?.length) return people.map(person => ({ label: personName(person), value: '', person }))
        // An iD the catalogue doesn't hold yet is read from ORCID's registry: the editor already has the
        // iD, so this is the check ORCID asks for, not a search of its registry by name
        if (id) return fromOrcid(id)
        if (people === null) return [{ notice: 'The catalogue\'s people couldn\'t be searched just now.' }]
        return [{ notice: 'Nobody by that name has an ORCID in the catalogue yet. Paste their ORCID iD here, or enter their details below.' }]
      },
      render: item => item.person ? personOption(item.person) : orcidOption(item.orcidPerson),
      menuClass: 'person-autocomplete',
      select: ({ person, orcidPerson }) => {
        if (orcidPerson) {
          // ORCID's own name for them, which the editor can still change; their organisation isn't read
          if (orcidPerson.givenNames) fill(view, 'givenName', orcidPerson.givenNames)
          if (orcidPerson.familyName) fill(view, 'familyName', orcidPerson.familyName)
          fill(view, 'nameIdentifier', orcidPerson.orcid)
          return
        }
        fill(view, 'givenName', person.givenName)
        fill(view, 'familyName', person.familyName)
        fill(view, 'nameIdentifier', person.orcid)
        const [affiliation] = person.affiliations || []
        if (affiliation && !model.get('organisationName')) {
          fill(view, 'organisationName', affiliation.name)
          if (affiliation.identifier) fill(view, 'organisationIdentifier', affiliation.identifier)
        }
      }
    })
  }

  // Beneath an empty iD, anyone the catalogue already holds an iD for by the name typed
  const suggestions = element('div', 'orcid-suggestions form-text')
  suggestions.hidden = true
  status.after(suggestions)
  let asked = 0

  const suggest = async () => {
    const asking = ++asked
    const familyName = model.get('familyName')
    if (model.get('nameIdentifier') || !familyName) {
      suggestions.hidden = true
      return
    }
    const people = await findPeople(personName({ givenName: model.get('givenName'), familyName }))
    if (asking !== asked) return
    const namesakes = (people || [])
      .filter(person => sameName(personName(person), model.get('givenName'), familyName))
      .slice(0, 3)
    suggestions.replaceChildren()
    suggestions.hidden = !namesakes.length
    if (!namesakes.length) return
    suggestions.append(element('p', 'mb-1', 'The catalogue already has an ORCID for someone of this name. Use it only if it is the same person:'))
    namesakes.forEach(person => {
      const row = element('div', 'orcid-suggestion')
      const use = element('button', 'btn btn-sm btn-outline-secondary', 'Use this iD')
      use.type = 'button'
      use.setAttribute('aria-label', `Use ${person.orcid} for ${personName(person)}`)
      use.addEventListener('click', () => fill(view, 'nameIdentifier', person.orcid))
      row.append(use, personOption(person))
      suggestions.append(row)
    })
  }
  view.listenTo(model, 'change:nameIdentifier change:givenName change:familyName', suggest)
  suggest()
}
