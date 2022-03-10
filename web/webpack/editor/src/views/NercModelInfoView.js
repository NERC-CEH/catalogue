import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import template from '../templates/NercModelInfo.tpl'

export default ObjectInputView.extend({

  template,

  initialize () {
    this.template = _.template(template)
    ObjectInputView.prototype.initialize.apply(this)
    const catalogue = $('html').data('catalogue')

    this.$('.autocomplete').autocomplete({
      minLength: 0,
      source (request, response) {
        let query
        const term = request.term.trim()
        if (_.isEmpty(term)) {
          query = `/${catalogue}/documents?term=documentType:nerc-model`
        } else {
          query = `/${catalogue}/documents?term=documentType:nerc-model AND ${request.term}`
        }

        return $.getJSON(query, data => response(_.map(data.results, d => ({
          value: d.title,
          label: d.title,
          identifier: d.identifier
        }))))
      }
    })

    return this.$('.autocomplete').on('autocompleteselect', (event, ui) => {
      this.model.set('id', ui.item.identifier)
      return this.$('.identifier').val(ui.item.identifier)
    })
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select.spatial-data').val(this.model.get('availableSpatialData'))
    return this
  }
})
