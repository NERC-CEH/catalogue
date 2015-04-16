define [
  'underscore'
  'backbone'
  'cs!models/SearchPage'
], (_, Backbone, SearchPage) -> Backbone.Model.extend

  defaults:
    drawing:   false
    mapsearch: false
    bbox:      undefined
    op:        undefined
    facet:     []
    term:      undefined
    page:      1

  ###
  Define the set of fields which contribute to searching
  ###
  searchFields: ['term', 'page', 'facet', 'bbox', 'op']

  initialize:->
    do @createSearchPage          #Create initial search page
    @on 'change', @jumpToPageOne
    @on 'change', @disableDrawing
    @on 'change', @performSearch

  ###
  Define a new search page which proxies its events through the this model.
  This means that views (and other models) can listen to the events of this 
  model rather than having to manually keep track of the current search page.
  Events from old search pages will be suppressed, only the current events will
  propergate out.

  To bind to a search prefix the event listener with 'results-'. E.g. 
      results-change:selected

  ###
  createSearchPage: ->
    @_proxyResultsEvents = (evt) => @trigger "results-#{evt}"
    @results = new SearchPage [], {}
    @results.on 'all', @_proxyResultsEvents

  ###
  On certain changes we want to switch to the first page. For example, if the
  term has changed, we won't want to fetch a middle search page. Since this 
  method is fired from an event listener which is registered before any other 
  listeners, then catch all 'change' listeners will only fire once.
  ###
  jumpToPageOne:(evt)-> @set('page', @defaults.page) unless evt.changed.page

  ###
  If any aspect of the model is updated we will want to disable drawing. 
  Imagine you have toggled drawing and then begin to start typing a new term,
  a new result may be shown on the map. You would expect to be able to pan and
  zoom into the new result.
  ###
  disableDrawing:(evt) -> @set('drawing', false) unless evt.changed.drawing

  ###
  Perform a search based upon the currently set properties of this model. Only
  fetch the results if the change event contains search related fields
  ###
  performSearch:(evt) ->
    if not _.chain(evt.changed).pick(@searchFields...).isEmpty().value()
      do @clearResults     # Make sure that the results have been cleared    
      do @createSearchPage # Redefine a new search page
      @results.fetch cache: false, traditional:true, data: @getState()

  ###
  Returns the current search state of this model. This method will generate
  an object which can be used for querying the search api.
  ###
  getState: ->
    state = @pick @searchFields...
    _.each state, (v, k) -> delete state[k] if not v
    return state

  ###
  Updates this model with the given state object. The state object only 
  represents the query aspect of the application. Additional options can be
  passed to the @set method. If properties are omitted, grab these from 
  @defaults
  ###
  setState: (state, options) ->
    searchDefaults = _.pick @defaults, @searchFields...
    @set _.defaults(state, searchDefaults), options

  ###
  Returns the current results set which this app has fetched or is fetching.
  ###
  getResults: -> @results

  ###
  Remove the search results object and remove the proxied event listener. 
  This method will trigger a 'cleared:results' event
  ###
  clearResults:-> 
    @results?.off null, @_proxyResultsEvents
    @results = null
    @trigger 'cleared:results'