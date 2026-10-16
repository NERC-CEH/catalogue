import _ from 'underscore'
import $ from 'jquery'
import template from '../templates/Relationship'
import ObjectInputView from './ObjectInputView'
import 'jquery-ui/ui/widgets/autocomplete'

// constrain autocomplete menu so that it does not exceed the width of the associate input field
if ($.ui && $.ui.autocomplete && $.ui.autocomplete.prototype) {
  $.ui.autocomplete.prototype._resizeMenu = function () {
    const ul = this.menu.element
    ul.outerWidth(this.element.outerWidth())
  }
}

// The catalogue's own record URIs, as relationship targets are often stored
const ownRecord = /^https?:\/\/catalogue\.ceh\.ac\.uk\/(?:id|documents)\/([^/?#]+)$/

/**
 * Where to fetch a relationship's target from, or null when it isn't fetched. A target is stored as a
 * record id or as a URI. The catalogue's own URIs are fetched from this catalogue, like an id, so that
 * staging and local stacks don't ask production (which their origin isn't allowed to read).
 *
 * Any other URL is shown as stored and never requested. A target is saved as given, through the API
 * too, so it can be any address, and fetching it ran that site's script in the editor of whoever
 * opened the record: jQuery makes a JSON request to a URL with `=?` in it JSONP (dri-one #441).
 */
export function recordUrl (target) {
  const own = ownRecord.exec(target)
  if (own) return `/documents/${encodeURIComponent(own[1])}`
  return /^[\w-]+$/.test(target) ? `/documents/${target}` : null
}

/**
 * What the read-only field says about a relationship's target: its title, type and id, or, when they
 * can't be shown, the target as stored and why. A missing record and one the editor can't see get one
 * message, as the save check (RelationshipTargets) gives, so as not to say which; any other failure
 * isn't reported as a broken link (dri-one #465). The stored target is kept, to be saved or removed.
 */
export async function describeTarget (target) {
  const url = recordUrl(target)
  if (!url) return { text: target }
  try {
    // Never JSONP, whatever the address: $.getJSON would turn `=?` into a script
    const data = await $.ajax({ url, dataType: 'json', jsonp: false })
    return { text: `${data.title} (${data.type}, ${data.id})` }
  } catch (error) {
    if (error?.status === 404 || error?.status === 403) {
      return { text: target, problem: 'Record not found, or you don\'t have access to it', missing: true }
    }
    return { text: target, problem: 'Couldn\'t load the record\'s details' }
  }
}

// The rules for what each relationship may point at live in the backend (RelationshipRules),
// which also enforces them on save. Fetched once per record type.
const rulesByType = new Map()

// The rules couldn't be fetched. The search isn't sent unfiltered then: it offered records the save
// rejects, from the record's own catalogue only, and hid that the rules were missing (on staging and
// production the proxy had no route for them)
export const RULES_UNAVAILABLE = Symbol('rules unavailable')

export function relationshipRules (type) {
  const key = type || ''
  if (!rulesByType.has(key)) {
    const rules = Promise.resolve($.getJSON(`/relationships/rules?type=${encodeURIComponent(key)}`))
      .catch(error => {
        console.error('Error fetching relationship rules:', error)
        // Asked for again on the next search
        rulesByType.delete(key)
        return RULES_UNAVAILABLE
      })
    rulesByType.set(key, rules)
  }
  return rulesByType.get(key)
}

// What the menu says when it has nothing to offer. Shown as an item that can't be chosen.
const NOTICES = {
  [RULES_UNAVAILABLE]: 'What this relationship can link to couldn\'t be loaded. Try again, or reload the page.',
  none: 'No matching records',
  failed: 'The search failed. Try again.'
}

// Catalogue titles by id, for showing where each search result comes from. Fetched once.
let catalogueTitles = null

function cataloguesById () {
  if (!catalogueTitles) {
    catalogueTitles = Promise.resolve($.getJSON('/catalogues'))
      .then(catalogues => Object.fromEntries((catalogues || []).map(c => [c.id, c.title])))
      .catch(error => {
        // Fall back to showing the catalogue id
        console.error('Error fetching catalogues:', error)
        catalogueTitles = null
        return {}
      })
  }
  return catalogueTitles
}

/**
 * One search result in the picker. A relationship such as "Produced at" searches other
 * catalogues than the record's own, so each result says which catalogue it comes from.
 * Record titles are escaped: the menu item is rendered as HTML.
 */
export async function resultItem (d) {
  const titles = await cataloguesById()
  const catalogue = titles[d.catalogue] || d.catalogue
  // Shares the identifier's secondary line (every span in a menu item is one, see editor.scss)
  const detail = [catalogue, d.identifier].filter(Boolean).map(_.escape).join(' · ')
  return {
    value: d.identifier,
    label: d.title,
    html: `${_.escape(d.title)} (${_.escape(d.resourceType)}) <span>${detail}</span>`
  }
}

// For tests: forget fetched rules and catalogues
export function clearRelationshipRules () {
  rulesByType.clear()
  catalogueTitles = null
}

// A Gemini record keeps its type in resourceType, which the editor can change; other records in
// type. SingleObjectView stores resourceType as plain data.
export function recordType (model) {
  return model?.get('resourceType')?.value || model?.get('type')
}

/**
 * The record search for one relationship. A restricted relationship filters on resourceType,
 * which Solr holds as the case-sensitive codelist label (e.g. "Dataset", not "dataset"), and may
 * search other catalogues than the record's own through the cross-catalogue endpoint. Link
 * documents are never offered, whatever the relationship. Null when a
 * restricted relationship has no type it could match, and RULES_UNAVAILABLE when the rules couldn't
 * be fetched. The rules leave out empty lists.
 */
export async function searchQuery ({ catalogue, relation, sourceType, currentId, searchTerm }) {
  const rules = await relationshipRules(sourceType)
  if (rules === RULES_UNAVAILABLE) return RULES_UNAVAILABLE
  const targets = rules[relation]
  const clauses = []
  if (targets) {
    const labels = targets.resourceTypes ?? []
    if (!labels.length) {
      // Restricted, but nothing can match yet: e.g. "Replaces" before the record has a type
      return null
    }
    clauses.push(`resourceType:(${labels.map(label => `"${label}"`).join(' OR ')})`)
  }
  const catalogues = targets?.catalogues ?? []
  if (catalogues.length) {
    // A record shared into a catalogue carries it in catalogue_view, not catalogue
    clauses.push(`(${catalogues.map(c => `catalogue:${c} OR catalogue_view:${c}`).join(' OR ')})`)
  }
  clauses.push(searchTerm ? `(${searchTerm})` : '*')
  // Link documents are being retired; a relationship belongs on the record one stands in for,
  // and the save-time check rejects them
  clauses.push('NOT documentType:LINK_DOCUMENT')
  if (currentId) {
    clauses.push(`NOT identifier:${currentId}`)
  }
  const endpoint = catalogues.length ? '/documents' : `/${catalogue}/documents`
  return `${endpoint}?term=${encodeURIComponent(clauses.join(' AND '))}`
}

export default ObjectInputView.extend({

  // What a relationship means goes beneath its name, in a span the styled select sets on a second
  // line and leaves out of the closed select. Each is escaped on its own, the span being markup.
  optionTemplate: _.template(
    '<option value="<%- value %>" <%-selected%>><%- label %><% if (description) { %><span><%- description %></span><% } %></option>'
  ),

  async initialize (options) {
    this.template = template
    this.options = options.options
    this.resourceType = options.resourceType
    this.parentModel = options.parentModel

    ObjectInputView.prototype.initialize.call(this, options)

    const catalogue = $('html').data('catalogue')

    const autocomplete = this.$('.autocomplete').autocomplete({
      minLength: 2,

      source: async (request, response) => {
        const query = await searchQuery({
          catalogue,
          relation: this.$('.relationshipList').val(),
          sourceType: recordType(this.parentModel),
          currentId: this.parentModel?.get('id'),
          searchTerm: request.term.trim()
        })

        if (query === RULES_UNAVAILABLE) {
          response([{ notice: NOTICES[RULES_UNAVAILABLE] }])
          return
        }
        if (!query) {
          response([{ notice: NOTICES.none }])
          return
        }

        try {
          const options = await $.getJSON(query)
          const items = await Promise.all(_.map(options.results, resultItem))

          // An empty menu looked like no search had been made
          response(items.length ? items : [{ notice: NOTICES.none }])
        } catch (error) {
          console.error('Error fetching data:', error)
          // Always answer, or the autocomplete stays in its loading state
          response([{ notice: NOTICES.failed }])
        }
      },

      select: async (event, ui) => {
        if (ui.item.notice) {
          event.preventDefault()
          return
        }
        this.$('.title').val(ui.item.label)
        this.$('.identifier').val(ui.item.value)
        this.showTarget(await describeTarget(ui.item.value))

        this.$('.relationshipList').prop('disabled', true)
        this.$('.relationshipSearch').addClass('d-none')
        this.$('.relationshipRecord').removeClass('d-none')
      }
    })

    autocomplete.autocomplete('widget')
      .addClass('relationship-autocomplete')

    autocomplete.autocomplete('instance')._renderItem = function (ul, item) {
      // jQuery UI's menu skips a disabled item, so a notice can't be chosen
      if (item.notice) {
        return $('<li class="ui-state-disabled relationship-notice">').append($('<div>').text(item.notice)).appendTo(ul)
      }
      return $('<li>')
        .append($('<div>').html(item.html))
        .appendTo(ul)
    }

    autocomplete.attr('placeholder', 'Choose a relationship type first')

    // Disable until a relationship is chosen
    this.$('.autocomplete').prop('disabled', true)

    this.$('.relationshipList').on('change', (e) => {
      const relationship = $(e.currentTarget).val()
      this.$('.autocomplete')
        .prop('disabled', !relationship)
        .attr('placeholder', relationship ? 'Enter record ID or type to search…' : 'Choose a relationship type first')
    })

    const target = this.model.get('target')

    if (!_.isEmpty(target)) {
      this.existingRecord = true
      await this.render()
    }
  },

  // Set with .val(), which raises no change event, so the stored target isn't touched. The title
  // gives the whole text, which a narrow field cuts short; a problem goes beneath it, as the field's
  // own text would be cut off. A missing record is marked as Bootstrap marks an invalid field.
  showTarget ({ text, problem, missing = false }) {
    this.$('.read-only-identifier').val(text).attr('title', text).toggleClass('is-invalid', missing)
    this.$('.target-problem')
      .text(problem || '')
      .prop('hidden', !problem)
      .toggleClass('invalid-feedback d-block', missing)
      .toggleClass('form-text', Boolean(problem) && !missing)
  },

  async render () {
    ObjectInputView.prototype.render.apply(this)

    if (this.existingRecord) {
      const description = await describeTarget(this.model.get('target'))

      this.$('.relationshipList').prop('disabled', true)
      this.showTarget(description)
      this.$('.relationshipRecord').removeClass('d-none')
      this.$('.relationshipSearch').addClass('d-none')
    }

    if (
      !this.model.attributes.relation &&
      !this.options.some(o => o.value === '')
    ) {
      this.options.unshift({
        value: '',
        label: 'Choose a relationship'
      })
    }

    this.options.forEach(option => {
      option.selected =
        (option.value === this.model.attributes.relation ||
          option.value === '')
          ? 'selected'
          : ''

      this.$('.relationshipList').append(this.optionTemplate({ description: '', ...option }))
    })

    return this
  }
})
