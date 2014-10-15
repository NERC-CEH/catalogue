define [
  'jquery'
  'cs!models/MapViewerApp'
  'cs!views/MapViewerAppView'
  'cs!models/SearchApp'
  'cs!views/SearchAppView'
  'cs!routers/LayersRouter'
  'cs!routers/SearchRouter'
  'cs!enables/CopyToClipboard'
  'bootstrap'
], ($, MapViewerApp, MapViewerAppView, SearchApp, SearchAppView, LayersRouter, SearchRouter) ->
  
  ###
  This is the initalizer method for the entire requirejs project. Here we can
  set up the different applications and initialize any javascript code which
  we like globally.
  ###
  initialize: ->
    do @initMapviewer if $('#mapviewer').length
    do @initSearch if $('#search').length
      
  ###
  Initialize the map viewer app, view and router
  ###
  initMapviewer: ->
    app = new MapViewerApp();
    view = new MapViewerAppView model: app

    router = new LayersRouter model: app
    do Backbone.history.start

  ###
  Initialize the search application
  ###
  initSearch: ->
    window.app = new SearchApp();
    window.view = new SearchAppView model: window.app

    router = new SearchRouter model: app, location: window.location
    
    do Backbone.history.start