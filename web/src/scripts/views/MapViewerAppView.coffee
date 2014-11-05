define [
  'jquery'
  'backbone'
  'cs!views/MapViewerMapView'
  'cs!views/SortableCollectionView'
  'cs!views/LayerControlsView'
], ($, Backbone, MapViewerMapView, SortableCollectionView, LayerControlsView) -> Backbone.View.extend
  el: '#mapviewer'

  initialize: ->
    do @render

  render: ->
    @mapViewerMapView = new MapViewerMapView
      model: @model
      el: @$('.openlayers')

    @layersView = new SortableCollectionView
      collection: @model.getLayers()
      el: @$('.layers .list-group')
      attributes:
        subView: LayerControlsView