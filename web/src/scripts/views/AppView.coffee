define [
  'jquery'
  'backbone'
  'cs!views/OpenLayersView'
], ($, Backbone, OpenLayersView) -> Backbone.View.extend
  el: '#mapviewer'

  initialize: ->
    do @render

  render: ->
    @openlayersView = new OpenLayersView
      model: @model
      el: @$('.openlayers')