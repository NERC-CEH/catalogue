define [
  'jquery'
  'cs!models/MapViewerApp'
  'cs!views/MapViewerAppView'
  'cs!routers/LayersRouter'
], ($, MapViewerApp, MapViewerAppView, LayersRouter) ->
  
  initialize: ->
    if $('#mapviewer').length
      app = new MapViewerApp();
      view = new MapViewerAppView model: app

      router = new LayersRouter model: app
      Backbone.history.start();