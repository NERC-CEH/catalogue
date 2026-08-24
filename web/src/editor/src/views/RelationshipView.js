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

    // Current document details
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

          // resourceType is indexed as the codelist display label, so a label
          // containing a space has to be quoted or the Solr query breaks.
          'http://purl.org/cerif/frapo/hasOutput': () =>
            `${encodedTerm}&facet=recordType%7C(Model%20OR%20Dataset%20OR%20Map%20(web%20service)%20OR%20Software%20OR%20Non-geographic%20dataset)`,

          'http://purl.org/dc/terms/isPartOf': () => {
            if (currentResourceType === 'dataset') {
              return `resourceType%3AAggregation%20AND%20${encodedTerm}`
            }

            if (currentResourceType === 'Monitoring facility') {
              return 'resourceType%3A%22Monitoring%20network%22%20AND%20' + encodedTerm
            }

            return encodedTerm
          }
        }

        let query

        if (!searchTerm) {
          query = `/${catalogue}/documents`
        } else if (
          selectedRelationship.startsWith(
            'https://digital.ceh.ac.uk/ontology/doo/hasChild'
          )
        ) {
          query = `/${catalogue}/documents?term=resourceType%3A%22${encodeURIComponent(currentResourceType)}%22%20AND%20${encodedTerm}`
        } else {
          const termQuery = relationshipQueries[selectedRelationship]?.() ?? encodedTerm
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
