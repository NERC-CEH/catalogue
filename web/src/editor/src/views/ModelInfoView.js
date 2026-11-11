import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import template from '../templates/ModelInfo'
import { autocomplete } from '../utils/autocomplete'

export default ObjectInputView.extend({

  initialize () {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this, arguments)
    const catalogue = $('html').data('catalogue')

    this.$('.autocomplete').each((i, input) => autocomplete(input, {
      minLength: 0,
      async source (term) {
        term = term.trim()
        // The term is a Solr query of its own: bracketed, so an OR in it can't widen the type filter
        const solrQuery = _.isEmpty(term) ? 'documentType:CEH_MODEL' : `documentType:CEH_MODEL AND (${term})`
        try {
          const data = await $.getJSON(`/${catalogue}/documents?term=${encodeURIComponent(solrQuery)}`)
          return _.map(data.results, d => ({
            value: d.title,
            label: d.title,
            identifier: d.identifier
          }))
        } catch {
          return []
        }
      },
      select: item => {
        this.model.set('id', item.identifier)
        this.$('.identifier').val(item.identifier)
      }
    }))
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select.spatial-data').val(this.model.get('availableSpatialData'))
    return this
  }
})
