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
    @on 'change:term change:bbox', @performSearch

  ###
  Perform a search based upon the currently set properties of this model.
  ###
  performSearch:->
    do @clearResults # Make sure that the results have been cleared 

    results = new SearchPage    
    @set 'results', results

    results.fetch
      remove: true
      cache:  false
      data:
        bbox: @get 'bbox'
        term: @get 'term'

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