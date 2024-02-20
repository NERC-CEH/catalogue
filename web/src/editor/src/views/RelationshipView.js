import _ from 'underscore'
import $ from 'jquery'
import template from '../templates/Relationship'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({

  optionTemplate: _.template('<option value="<%= value %>"><%= label %></option>'),

  initialize (options) {
    this.template = template
    this.options = options.options
    ObjectInputView.prototype.initialize.call(this, options)
    const catalogue = $('html').data('catalogue')

    this.$('.autocomplete').autocomplete({
      minLength: 2,
      source: (request, response) => {
        let query
        const term = request.term.trim()
        if (_.isEmpty(term)) {
          query = `/${catalogue}/documents`
        } else {
          query = `/${catalogue}/documents?term=${request.term}`
        }

        $.getJSON(query, data => response(_.map(data.results, d => ({
          value: d.identifier,
          label: d.title
        }))))
      },
      select: (event, ui) => {
        this.$('.title').val(ui.item.label)
        this.$('.identifier').val(ui.item.value)
        this.$('.relationshipSearch').addClass('hidden')
        this.$('.relationshipRecord').removeClass('hidden')
      }
    })

    const relId = this.model.get('identifier')
    if (relId != null) {
      this.$('.relationshipSearch').addClass('hidden')
      this.$('.relationshipRecord').removeClass('hidden')
    }
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.options.forEach(option => {
      return this.$('datalist').append(this.optionTemplate(option))
    })
    return this
  }
})
