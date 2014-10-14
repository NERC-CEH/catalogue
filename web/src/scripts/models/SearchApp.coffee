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

  initialize:->
    # Create a empty search page, 
    @results = new SearchPage [], {}

    # Listen to all the events which mean that a search should be performed
    do @proxyResultsEvents
    @on 'change:facets change:term change:spatialSearch', @jumpToPageOne
    @on 'change', @performSearch

  ###
  On certain changes we want to switch to the first page. For example, if the
  term has changed, we won't want to fetch a middle search page. Since this 
  method is fired from an event listener which is registered before any other 
  listeners, then catch all 'change' listeners will only fire once.
  ###
  jumpToPageOne:-> @set 'page', 1

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
    @results?.on 'all', (evt) => @trigger "results-#{evt}"

  ###
  Perform a search based upon the currently set properties of this model.
  ###
  performSearch:->
    do @clearResults # Make sure that the results have been cleared 

    @results = new SearchPage    
    do @proxyResultsEvents

    bbox = @get 'bbox'
    @results.fetch
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
  setBBox: (bbox) -> @set 'bbox', bbox, silent: not @get 'spatialSearch'

  ###
  Returns the current results set which this app has fetched or is fetching.
  ###
  getResults: -> @results

  ###
  Remove the search results object and remove the proxied event listener. 
  This method will trigger a 'cleared:results' event
  ###
  clearResults:-> 
    @results?.off null, @proxyResultsEvents
    @results = null
    @trigger 'cleared:results'