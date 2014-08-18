define [
  'jquery'
  'backbone'
  'cs!views/OpenLayersView'
  'cs!views/LayersControlPanelView'
], ($, Backbone, OpenLayersView, LayersControlPanelView) -> Backbone.View.extend
  el: '#mapviewer'

  initialize: ->
    do @render

  render: ->
    @openlayersView = new OpenLayersView
      model: @model
      el: @$('.openlayers')

    @layersView = new LayersControlPanelView
      collection: @model.getLayers()
      el: @$('.layers .list-group')