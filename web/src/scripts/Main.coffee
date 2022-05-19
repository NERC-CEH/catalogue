define [
  'underscore'
  'jquery'
  'backbone'
  'bootstrap'
  'cs!models/MapViewerApp'
  'cs!views/MapViewerAppView'
  'cs!routers/LayersRouter'
], (
  _, $, Backbone, Bootstrap, MapViewerApp, MapViewerAppView, LayersRouter,ChartView
) ->

  ###
  This is the initializer method for the entire requirejs project. Here we can
  set up the different applications and initialize any javascript code which
  we like globally.
  ###
  initialize: ->
    # shim
    # http://stackoverflow.com/a/646643
    String::startsWith ?= (s) -> @slice(0, s.length) == s
    String::endsWith   ?= (s) -> s == '' or @slice(-s.length) == s

    # Fix for underscore not being global
    # Some templates use underscore
    # Remove once templates fixed
    window._ = _

    do @initMapviewer if $('#mapviewer').length

    $('.chart').each (i, e) -> new ChartView el: e
    do Backbone.history.start

  ###
  Initialize the WMS map viewer
  ###
  initMapviewer: ->
    app    = new MapViewerApp()
    view   = new MapViewerAppView model: app
    router = new LayersRouter model: app



