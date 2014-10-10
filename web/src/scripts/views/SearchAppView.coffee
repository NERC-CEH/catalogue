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
      model: @model
      el:    @$('.results')
