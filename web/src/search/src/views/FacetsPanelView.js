import Backbone from 'backbone'
import panelTpl from '../templates/facetsPanelTemplate'
import resultsTpl from '../templates/facetResultsTemplate'

export default Backbone.View.extend({

    initialize () {
        this.listenTo(this.model, 'results-sync', this.render)
    },

    /*
    Render the facet results panel as long as we have some results currently set.

    The template panelTpl requires a sub template which renders each facet
    results set.
    */
    render () {
        this.$el.html(panelTpl({
            facets: this.model.getResults().attributes.facets,
            template: resultsTpl
        }))
        return this
    }
})
