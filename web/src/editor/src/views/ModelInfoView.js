import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import template from '../templates/ModelInfo'

export default ObjectInputView.extend({

  initialize () {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this, arguments)
    const catalogue = $('html').data('catalogue')

    this.$('.autocomplete').autocomplete({
      minLength: 0,
      source (request, response) {
        const term = request.term.trim()
        // The term is a Solr query of its own: bracketed, so an OR in it can't widen the type filter
        const solrQuery = _.isEmpty(term) ? 'documentType:CEH_MODEL' : `documentType:CEH_MODEL AND (${term})`

        $.getJSON(`/${catalogue}/documents?term=${encodeURIComponent(solrQuery)}`)
          .done(data => response(_.map(data.results, d => ({
            value: d.title,
            label: d.title,
            identifier: d.identifier
          }))))
          .fail(() => response([]))
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
