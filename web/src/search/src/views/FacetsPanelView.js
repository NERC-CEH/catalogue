import Backbone from 'backbone'
import panelTpl from '../templates/facetsPanelTemplate'
import resultsTpl from '../templates/facetResultsTemplate'

export default Backbone.View.extend({

  initialize () {
    $.getJSON(window.location.href, data => {
      this.createFacetSearch(data.facets)
    })

    this.listenTo(this.model, 'results-sync', this.render)
  },

  createFacetSearch(facets) {
    facets.forEach(facet => {
      const id = "#search-facet-" + facet.displayName.replaceAll(' ', '-')
      if (typeof facet.results !== "undefined") {
        this.$(id).autocomplete({
          minLength: 1,
          source: facet.results.map(item => ({
            label: item.name,
            url: item.url
          })),
          select: (event, ui) => {
            this.$(id).val(ui.item.label)
            this.$("a[href='" + ui.item.url +"']").first().trigger("click")
            return false;
          }
        })
      }
    })
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

    this.createFacetSearch(facets)

    return this
  }
})
