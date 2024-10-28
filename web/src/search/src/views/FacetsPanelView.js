import Backbone from 'backbone'
import panelTpl from '../templates/facetsPanelTemplate'
import resultsTpl from '../templates/facetResultsTemplate'
import { createFacetSearch } from '../SearchFacets.js'

export default Backbone.View.extend({

  initialize () {
    this.listenTo(this.model, 'results-sync', this.render)
  },

  /*
     * Render the facet results panel as long as we have some results currently set.
     *
     * The template panelTpl requires a sub template which renders each facet
     * results set.
     */
  render () {
    const facets = this.model.getResults().attributes.facets
    this.$el.html(panelTpl({
      facets: facets,
      template: resultsTpl
    }))

    createFacetSearch(facets)

    return this
  }
})
