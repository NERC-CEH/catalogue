import Backbone from 'backbone'
import panelTpl from '../templates/FacetsPanel.tpl'
import resultsTpl from '../templates/FacetResults.tpl'
import _ from 'underscore'

export default Backbone.View.extend({

  initialize () {
    this.panelTpl = _.template(panelTpl)
    this.resultsTpl = _.template(resultsTpl)
    this.listenTo(this.model, 'results-sync', this.render)
  },

  /*
  Render the facet results panel as long as we have some results currently set.

  The template panelTpl requires a sub template which renders each facet
  results set.
  */
  render () {
    this.$el.html(this.panelTpl({
      facets: this.model.getResults().attributes.facets,
      template: this.resultsTpl
    }))
    return this
  }
})
