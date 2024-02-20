import _ from 'underscore'
import Backbone from 'backbone'
import SearchPage from './models/SearchPage'

export default Backbone.Model.extend({

  defaults: {
    drawing: false,
    mapsearch: false,
    bbox: undefined,
    op: 'intersects',
    facet: [],
    term: undefined,
    page: 1,
    rows: undefined
  },

  /*
     * Define the set of fields which contribute to searching
     */
  searchFields: ['term', 'page', 'rows', 'facet', 'bbox', 'op'],

  initialize () {
    this.createSearchPage() // Create initial search page
    this.on('change', this.jumpToPageOne)
    this.on('change', this.disableDrawing)
    this.on('change', this.performSearch)
  },

  /*
     * Define a new search page which proxies its events through the model.
     * This means that views (and other models) can listen to the events of this
     * model rather than having to manually keep track of the current search page.
     * Events from old search pages will be suppressed, only the current events will
     * propagate out.
     *
     * To bind to a search prefix the event listener with 'results-'. E.g.
     *         results-change:selected
     *
     */
  createSearchPage () {
    this._proxyResultsEvents = evt => this.trigger(`results-${evt}`)
    this.results = new SearchPage()
    this.results.on('all', this._proxyResultsEvents)
  },

  /*
     * On certain changes we want to switch to the first page. For example, if the
     * term has changed, we won't want to fetch a middle search page. Since this
     * method is fired from an event listener which is registered before any other
     * listeners, then catch all 'change' listeners will only fire once.
     */
  jumpToPageOne (evt) { if (!evt.changed.page) { this.set('page', this.defaults.page) } },

  /*
     * If any aspect of the model is updated we will want to disable drawing.
     * Imagine you have toggled drawing and then begin to start typing a new term,
     * a new result may be shown on the map. You would expect to be able to pan and
     * zoom into the new result.
     */
  disableDrawing (evt) { if (!evt.changed.drawing) { this.set('drawing', false) } },

  /*
     * Perform a search based upon the currently set properties of this model. Only
     * fetch the results if the change event contains search related fields
     */
  performSearch (evt) {
    if (!_.chain(evt.changed).pick(...Array.from(this.searchFields || [])).isEmpty().value()) {
      this.clearResults() // Make sure that the results have been cleared
      this.createSearchPage() // Redefine a new search page
      this.results.fetch({ cache: false, traditional: true, data: this.getState() })
    }
  },

  /*
     * Returns the current search state of this model. This method will generate
     * an object which can be used for querying the search api.
     */
  getState () {
    const state = this.pick(...Array.from(this.searchFields || []))
    _.each(state, function (v, k) { if (!v) { return delete state[k] } })
    return state
  },

  /*
     * Updates this model with the given state object. The state object only
     * represents the query aspect of the application. Additional options can be
     * passed to the @set method. If properties are omitted, grab these from
     * @defaults
     */
  setState (state, options) {
    const searchDefaults = _.pick(this.defaults, ...Array.from(this.searchFields))
    this.set(_.defaults(state, searchDefaults), options)
  },

  /*
     * Returns the current results set which this app has fetched or is fetching.
     */
  getResults () { return this.results },

  /*
     * Remove the search results object and remove the proxied event listener.
     * This method will trigger a 'cleared:results' event
     */
  clearResults () {
    if (this.results != null) {
      this.results.off(null, this._proxyResultsEvents)
    }
    this.results = null
    this.trigger('cleared:results')
  },
  /*
  Update the search box, which triggers an update of the results
   */
  setBbox(bbox) {
    this.set({'bbox': bbox})
  },

  /*
  Clear the search box, which triggers an update of the results
  Also clear the 'op', since this is the spatial operation to perform, which is not needed if there is no bbox
   */
  clearBbox() {
    this.unset('bbox')
  }
})
