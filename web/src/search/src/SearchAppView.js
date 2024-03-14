import $ from 'jquery'
import Backbone from 'backbone'
import deparam from 'deparam.js'
import { FacetsPanelView, SearchFormView, SearchPageView, SpatialFilterView } from './views'

export default Backbone.View.extend({
  el: '#search',

  events: {
    'click .accordion-item': 'updateMapsearch'
  },

  initialize (options) {
    this.appUrl = window.location.href.split('#')[0].split('?')[0]

    // Mutate the events hash so that is listens to clicks of urls which will
    // update the state of this web application
    this.events["click a[href='#{this.appUrl}']"] = 'defaultState'
    this.events["click a[href='#{this.appUrl}?']"] = 'handleUrl'

    this.delegateEvents(this.events) // Register the mutated events object

    this.render()
  },

  /*
     * Grab the query state from the anchors clicked href. Use this to update the
     * models state
     */
  handleUrl (e) {
    const query = $(e.currentTarget).attr('href').split('?')[1]
    this.model.setState(deparam(query, true))
    e.preventDefault()
  },

  /*
     * Set an empty state object on the model. This will revert the application to
     * the default state.
     */
  defaultState (e) {
    this.model.setState({})
    e.preventDefault()
  },

  updateMapsearch (e) {
    const targetClass = 'accordion-map'
    const isMapSearch = e.currentTarget.classList.contains(targetClass)
    this.model.set('mapsearch', isMapSearch)
  },

  /*
     * Create the sub views of the search web application
     */
  render () {
    this.searchFormView = new SearchFormView({
      model: this.model,
      el: this.$('.search-form')
    })
    this.searchResultsView = new SearchPageView({
      model: this.model,
      el: this.$('.results')
    })
    this.facetsPanelView = new FacetsPanelView({
      model: this.model,
      el: this.$('.facet-filter')
    })
    this.spatialFilterView = new SpatialFilterView({
      model: this.model,
      el: this.$('.mapsearch')
    })
    return this
  }
})
