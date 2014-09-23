define [
  'jquery'
  'backbone'
  'cs!views/OpenLayersView'
  'cs!views/SearchResultsView'
], ($, Backbone, OpenLayersView, SearchResultsView) -> Backbone.View.extend
  el: '#search'

  initialize: ->
    do @render

  render: ->
    @openlayersView = new OpenLayersView
      model: @model
      el: @$('.openlayers')
      
    @searchResultsView = new SearchResultsView
      collection: @model.getSearchResults()
      el: @$('.results')