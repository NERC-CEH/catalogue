define [
  'jquery'
  'cs!models/MapViewerApp'
  'cs!views/MapViewerAppView'
  'cs!routers/LayersRouter'
], ($, MapViewerApp, MapViewerAppView, LayersRouter) ->
  
  ###
  This is the initalizer method for the entire requirejs project. Here we can
  set up the different applications and initialize any javascript code which
  we like globally.
  ###
  initialize: ->
    do @initMapviewer if $('#mapviewer').length
      
  ###
  Initialize the map viewer app, view and router
  ###
  initMapviewer: ->
    app = new MapViewerApp();
    view = new MapViewerAppView model: app

    router = new LayersRouter model: app
    Backbone.history.start();
