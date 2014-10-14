define [
  'underscore'
  'backbone'
  'cs!models/SearchPage'
], (_, Backbone, SearchPage) -> Backbone.Model.extend

  defaults:
    spatialSearch: false
    facets:        []
    term:          ''
    page:          1
    results:       new SearchPage [], {}

  initialize:->
    # Listen to all the events which mean that a search should be performed
    do @proxyResultsEvents
    @on 'change:term change:bbox change:spatialSearch change:page', @performSearch

  ###
  Proxy the current results objects events through the search application model
  This means that views (and other models) can listen to the events of this 
  model rather than having to manually keep track of the current search page.
  Events from old search pages will be suppressed, only the current events will
  propergate out.

  To bind to a search prefix the event listener with 'results-'. E.g. 
      results-change:selected

  ###
  proxyResultsEvents:-> 
    @get('results')?.on 'all', (evt) => @trigger "results-#{evt}"

  ###
  Perform a search based upon the currently set properties of this model.
  ###
  performSearch:->
    do @clearResults # Make sure that the results have been cleared 

    results = new SearchPage    
    @set 'results', results
    do @proxyResultsEvents

    bbox = @get 'bbox'
    results.fetch
      remove: true
      cache:  false
      data:
        bbox: if @get 'spatialSearch' then @get 'bbox' 
        term: @get 'term'
        page: @get 'page'

  ###
  Sets the spatial search bounding box. The change event will be silenced if
  the spatialSearch property is false  
  ###
  setBBox: (bbox) ->
    @set 'bbox', bbox, silent: not @get 'spatialSearch'

  ###
  Remove the search results object and remove the proxied event listener.
  ###
  clearResults:-> 
    @get('results')?.off null, @proxyResultsEvents
    @unset 'results'