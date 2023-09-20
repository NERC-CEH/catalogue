import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import template from '../templates/ModelInfo'

export default ObjectInputView.extend({

  initialize () {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this)
    const catalogue = $('html').data('catalogue')

    this.$('.autocomplete').autocomplete({
      minLength: 0,
      source (request, response) {
        let query
        const term = request.term.trim()
        if (_.isEmpty(term)) {
          query = `/${catalogue}/documents?term=documentType:CEH_MODEL`
        } else {
          query = `/${catalogue}/documents?term=documentType:CEH_MODEL AND ${request.term}`
        }

        return $.getJSON(query, data => response(_.map(data.results, d => ({
          value: d.title,
          label: d.title,
          identifier: d.identifier
        }))))
      }
    })

    this.$('.autocomplete').on('autocompleteselect', (event, ui) => {
      this.model.set('id', ui.item.identifier)
      this.$('.identifier').val(ui.item.identifier)
    })
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select.spatial-data').val(this.model.get('availableSpatialData'))
    return this
  }
})
