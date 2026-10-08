import _ from 'underscore'
import $ from 'jquery'
import template from '../templates/Relationship'
import ObjectInputView from './ObjectInputView'

// constrain autocomplete menu so that it does not exceed the width of the associate input field
if ($.ui && $.ui.autocomplete && $.ui.autocomplete.prototype) {
  $.ui.autocomplete.prototype._resizeMenu = function () {
    const ul = this.menu.element
    ul.outerWidth(this.element.outerWidth())
  }
}

async function generateInformationString (target) {
  // Records can be kept either as a full URI or simply a UID
  const urlRegEx = /^https?:\/\/(?!catalogue\.ceh\.ac\.uk\/documents)(\w+:?\w*)?(\S+)(:\d+)?(\/|\/([\w#!:.?+=&%-/]))?$/
  const isValidUrl = url => urlRegEx.test(url)
  const query = isValidUrl(target) ? target : `/documents/${target}`

  try {
    const data = await $.getJSON(query)
    return `${data.title} (${data.type}, ${data.id})`
  } catch (error) {
    return target
  }
}

// The rules for what each relationship may point at live in the backend (RelationshipRules),
// which also enforces them on save. Fetched once per record type.
const rulesByType = new Map()

export function relationshipRules (type) {
  const key = type || ''
  if (!rulesByType.has(key)) {
    const rules = Promise.resolve($.getJSON(`/relationships/rules?type=${encodeURIComponent(key)}`))
      .catch(error => {
        // Fall back to an unfiltered search; the save-time check still applies
        console.error('Error fetching relationship rules:', error)
        rulesByType.delete(key)
        return {}
      })
    rulesByType.set(key, rules)
  }
  return rulesByType.get(key)
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
// type. Once edited, resourceType is a ResourceType model rather than the plain object it loads as
// (SingleView.updateMetadataModel stores what the change event passes).
export function recordType (model) {
  const resourceType = model?.get('resourceType')
  return resourceType?.get?.('value') || resourceType?.value || model?.get('type')
}

/**
 * The record search for one relationship. A restricted relationship filters on resourceType,
 * which Solr holds as the case-sensitive codelist label (e.g. "Dataset", not "dataset"), and may
 * search other catalogues than the record's own through the cross-catalogue endpoint. Null when a
 * restricted relationship has no type it could match. The rules leave out empty lists.
 */
export async function searchQuery ({ catalogue, relation, sourceType, currentId, searchTerm }) {
  const targets = (await relationshipRules(sourceType))[relation]
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
  if (currentId) {
    clauses.push(`NOT identifier:${currentId}`)
  }
  const endpoint = catalogues.length ? '/documents' : `/${catalogue}/documents`
  return `${endpoint}?term=${encodeURIComponent(clauses.join(' AND '))}`
}

export default ObjectInputView.extend({

  optionTemplate: _.template(
    '<option value="<%= value %>" <%=selected%>><%= label %></option>'
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

        if (!query) {
          response([])
          return
        }

        try {
          const options = await $.getJSON(query)

          response(await Promise.all(_.map(options.results, resultItem)))
        } catch (error) {
          console.error('Error fetching data:', error)
          // Always answer, or the autocomplete stays in its loading state
          response([])
        }
      },

      select: async (event, ui) => {
        const infoString = await generateInformationString(ui.item.value)

        this.$('.title').val(ui.item.label)
        this.$('.identifier').val(ui.item.value)
        this.$('.read-only-identifier').val(infoString)

        this.$('.relationshipList').prop('disabled', true)
        this.$('.relationshipSearch').addClass('d-none')
        this.$('.relationshipRecord').removeClass('d-none')
      }
    })

    autocomplete.autocomplete('widget')
      .addClass('relationship-autocomplete')

    autocomplete.autocomplete('instance')._renderItem = function (ul, item) {
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

  async render () {
    ObjectInputView.prototype.render.apply(this)

    if (this.existingRecord) {
      const infoString =
        await generateInformationString(this.model.get('target'))

      this.$('.relationshipList').prop('disabled', true)
      this.$('.read-only-identifier').val(infoString)
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

      const label = option.description
        ? `${option.label}<span>${option.description}</span>`
        : option.label

      this.$('.relationshipList').append(
        this.optionTemplate({
          ...option,
          label
        })
      )
    })

    return this
  }
})
