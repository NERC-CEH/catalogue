import _ from 'underscore'
import $ from 'jquery'
import template from '../templates/Relationship'
import ObjectInputView from './ObjectInputView'

async function generateInformationString (target) {
  // Records can be kept either as a full URI or simply a UID
  const urlRegEx = /(?:https?):\/\/(\w+:?\w*)?(\S+)(:\d+)?(\/|\/([\w#!:.?+=&%!\-/]))?/
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

  optionTemplate: _.template('<option value="<%= value %>"><%= label %></option>'),

  async initialize (options) {
    this.template = template
    this.options = options.options
    ObjectInputView.prototype.initialize.call(this, options)
    const catalogue = $('html').data('catalogue')

    this.$('.autocomplete').autocomplete({
      minLength: 2,
      source: async (request, response) => {
        let query
        const term = request.term.trim()
        if (_.isEmpty(term)) {
          query = `/${catalogue}/documents`
        } else {
          query = `/${catalogue}/documents?term=${request.term}`
        }

        try {
          const options = await $.getJSON(query)
          response(_.map(options.results, d => ({
            value: d.identifier,
            label: d.title
          })))
        } catch (error) {
          console.error('Error fetching data:', error)
        }
      },
      select: async (event, ui) => {
        const infoString = await generateInformationString(ui.item.value)
        this.$('.title').val(ui.item.label)
        this.$('.identifier').val(ui.item.value)
        this.$('.read-only-identifier').val(infoString)

        this.$('.relationshipSearch').addClass('hidden')
        this.$('.relationshipRecord').removeClass('hidden')
      }
    })

    const target = this.model.get('target')
    if (!_.isEmpty(target)) {
      this.existingRecord = true
      this.render()
    }
  },

  async render () {
    ObjectInputView.prototype.render.apply(this)

    if (this.existingRecord) {
      const infoString = await generateInformationString(this.model.get('target'))
      this.$('.read-only-identifier').val(infoString)
      this.$('.relationshipRecord').removeClass('hidden')
      this.$('.relationshipSearch').addClass('hidden')
    }

    this.options.forEach(option => {
      return this.$('datalist').append(this.optionTemplate(option))
    })
    return this
  }
})
