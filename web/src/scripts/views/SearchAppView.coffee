define [
  'jquery'
  'backbone'
  'cs!views/SearchFormView'
  'cs!views/SpatialSearchView'
  'cs!views/SearchPageView'
  'cs!views/DrawingControlView'
], ($, Backbone, SearchFormView, SpatialSearchView, SearchPageView, DrawingControlView) -> Backbone.View.extend
  el: '#search'

  events:
    "click .facet-heading a" : "facetMode"
    "click .map-heading a" :   "mapMode"

  initialize: ->
    do @render

  # TODO these should set and read a value from the search app to dictate the 
  # mode to be in.
  facetMode :(e) -> 
    @$el.removeClass('map-mode').addClass 'facets-mode'
    do e.preventDefault

  mapMode: (e) ->
    @$el.removeClass('facets-mode').addClass 'map-mode'

    do e.preventDefault

  ###
  Create the sub views of the search web application
  ###
  render: ->
    @searchFormView = new SearchFormView
      model: @model
      el:    @$('.search-form')
      
    @spatialSearchView = new SpatialSearchView
      model: @model
      el:    @$('.openlayers')

    @searchResultsView = new SearchPageView
      model: @model
      el:    @$('.results')

    @drawingControlView = new DrawingControlView
      model: @model
      el:    @$('.map-heading button')
