define [
  'underscore'
  'backbone'
  'cs!models/SearchPage'
], (_, Backbone, SearchPage) -> Backbone.Model.extend

  defaults:
    spatialSearch: false
    facets:        []
    term:          ''
    results:       new SearchPage [], {}

  initialize:->
    # Listen to all the events which mean that a search should be performed
    @on 'change:term change:bbox change:spatialSearch', @performSearch

  ###
  Perform a search based upon the currently set properties of this model.
  ###
  performSearch:->
    do @clearResults # Make sure that the results have been cleared 

    results = new SearchPage    
    @set 'results', results

    bbox = @get 'bbox'
    results.fetch
      remove: true
      cache:  false
      data:
        bbox: if @get 'spatialSearch' then @get 'bbox' 
        term: @get 'term'

  ###
  Sets the spatial search bounding box. The change event will be silenced if
  the spatialSearch property is false  
  ###
  setBBox: (bbox) ->
    @set 'bbox', bbox, silent: not @get 'spatialSearch'

  ###
  Obtain the current page of search results. This will return undefined if a
  search is currently being performed
  ###
  getSearchResults: -> @get 'results'

  ###
  Remove the search results object 
  ###
  clearResults:-> @unset 'results'

  ###
  Checks to see if this search app currently has a results page
  ###
  hasResults: -> @has 'results'