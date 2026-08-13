import _ from 'underscore'
import $ from 'jquery'
import template from '../templates/Relationship'
import ObjectInputView from './ObjectInputView'

// constrain autocomplete menu so that it does not exceed the width of the associate input field
$.ui.autocomplete.prototype._resizeMenu = function () {
  const ul = this.menu.element
  ul.outerWidth(this.element.outerWidth())
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

export default ObjectInputView.extend({

  optionTemplate: _.template(
    '<option value="<%= value %>" <%= selected %>><%= label %></option>'
  ),

  async initialize (options) {
    this.template = template
    this.options = options.options
    this.resourceType = options.resourceType
    this.parentModel = options.parentModel

    ObjectInputView.prototype.initialize.call(this, options)

    const catalogue = $('html').data('catalogue')

    const recordTypes = {
      monitoringFacility: 'Monitoring facility',
      monitoringProgramme: 'Monitoring programme',
      monitoringNetwork: 'Monitoring network'
    }

    const currentId = this.parentModel?.get('id')
    const currentType = this.parentModel?.get('type')
    const currentResourceType = recordTypes[currentType] || currentType

    const autocomplete = this.$('.autocomplete').autocomplete({
      minLength: 2,

      source: async (request, response) => {
        const searchTerm = request.term.trim()
        const selectedRelationship = this.$('.relationshipList').val()

        const term = currentId
          ? `${searchTerm} AND NOT identifier:${currentId}`
          : searchTerm

        const encodedTerm = encodeURIComponent(term)

        const relationshipQueries = {
          'http://purl.org/dc/terms/replaces': () =>
            `resourceType%3A%22${encodeURIComponent(currentResourceType)}%22%20AND%20${encodedTerm}`,

          'http://purl.org/dc/terms/isPartOf': () => {
            if (currentResourceType === 'dataset') {
              return `resourceType%3AAggregation%20AND%20${encodedTerm}`
            }

            if (currentResourceType === 'Monitoring facility') {
              return `resourceType%3A%22Monitoring%20network%22%20AND%20${encodedTerm}`
            }

            return encodedTerm
          }
        }

        let query

        if (!searchTerm) {
          query = `/${catalogue}/documents`
        } else if (
          selectedRelationship &&
          selectedRelationship.startsWith(
            'https://digital.ceh.ac.uk/ontology/doo/hasChild'
          )
        ) {
          query =
            `/${catalogue}/documents?term=` +
            `resourceType%3A%22${encodeURIComponent(currentResourceType)}%22%20AND%20${encodedTerm}`
        } else {
          const termQuery =
            relationshipQueries[selectedRelationship]?.() ?? encodedTerm

          query = `/${catalogue}/documents?term=${termQuery}`
        }

        try {
          const options = await $.getJSON(query)

          response(
            _.map(options.results, d => ({
              value: d.identifier,
              label: d.title,
              html: `${d.title} (${d.resourceType}) <span>${d.identifier}</span>`
            }))
          )
        } catch (error) {
          console.error('Error fetching data:', error)
        }
      },

      select: async (event, ui) => {
        const infoString = await generateInformationString(ui.item.value)

        this.$('.title').val(ui.item.label)
        this.$('.identifier').val(ui.item.value)
        this.$('.read-only-identifier').val(infoString)

        // lock relationship after record selection
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

    const target = this.model.get('target')

    if (!_.isEmpty(target)) {
      this.existingRecord = true
      await this.render()
    }
  },

  async render () {
    ObjectInputView.prototype.render.apply(this)

    if (
      !this.model.attributes.relation &&
      !this.options.some(o => o.value === '')
    ) {
      this.options.unshift({ value: '', label: 'Choose a relationship type' })
    }

    this.$('.relationshipList').empty()

    this.options.forEach(option => {
      option.selected =
        (option.value === this.model.attributes.relation ||
         option.value === '')
          ? 'selected'
          : ''

      this.$('.relationshipList')
        .append(this.optionTemplate(option))
    })

    const autocompleteInput = this.$('.autocomplete')
    const relationshipList = this.$('.relationshipList')

    // Initial state
    autocompleteInput.attr('placeholder', 'Choose a relationship type first')
    autocompleteInput.prop('disabled', !relationshipList.val())

    // Prevent duplicate event bindings
    relationshipList.on('change.relationship', e => {
      const relationship = $(e.currentTarget).val()
      const enabled = !!relationship

      autocompleteInput.prop('disabled', !enabled)
      autocompleteInput.attr('placeholder', enabled ? 'Enter record ID or type to search…' : 'Choose a relationship type first')
    })


    if (this.existingRecord) {
      const infoString =
        await generateInformationString(this.model.get('target'))

      this.$('.read-only-identifier').val(infoString)

      this.$('.relationshipRecord').removeClass('d-none')
      this.$('.relationshipSearch').addClass('d-none')

      relationshipList.prop('disabled', true)
      autocompleteInput.prop('disabled', true)
    }

    return this
  }
})
