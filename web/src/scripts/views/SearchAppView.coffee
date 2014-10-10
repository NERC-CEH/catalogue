define [
  'jquery'
  'backbone'
  'cs!views/SearchFormView'
  'cs!views/SpatialSearchView'
  'cs!views/SearchPageView'
  'cs!views/ToggleView'
], ($, Backbone, SearchFormView, SpatialSearchView, SearchPageView, ToggleView) -> Backbone.View.extend
  el: '#search'

  initialize: ->
    do @render
    @listenTo @model, 'change:results', @handleSearching

  ###
  Search pages can be replaced with either new search pages or nothing.
  In the case of a new search page have to replace the old view with a
  new one. In the case of no results we just remove the view.
  ###
  handleSearching: ->
    do @searchResultsView.remove if @searchResultsView
    if @model.hasResults()
      @searchResultsView = new SearchPageView
        model: @model.getSearchResults()
        el: $('<div class="results"></div>').appendTo @$('.text-search')

  ###
  Create the sub views of the search web application
  ###
  render: ->
    @searchFormView = new SearchFormView
      model: @model
      el:    @$('.search-form')

    @spatialSearchToggleView = new ToggleView
      model:    @model
      el:       @$('.mapsearch-toggle')
      property: 'spatialSearch'
      
    @spatialSearchView = new SpatialSearchView
      model: @model
      el:    @$('.openlayers')

    @searchResultsView = new SearchPageView
      model: @model.getSearchResults()
      el:    @$('.results')
