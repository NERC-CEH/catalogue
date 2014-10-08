define [
  'jquery'
  'backbone'
  'cs!views/SpatialSearchView'
  'cs!views/SearchResultsView'
], ($, Backbone, SpatialSearchView, SearchResultsView) -> Backbone.View.extend
  el: '#search'

  initialize: ->
    do @render

  render: ->
    @spatialSearchView = new SpatialSearchView
      model: @model
      el: @$('.openlayers')
      
    @searchResultsView = new SearchResultsView
      collection: @model.getSearchResults()
      el: @$('.results')