define [
  'underscore'
  'jquery'
  'backbone'
  'bootstrap'
  'cs!views/StudyAreaView'
  'cs!models/MapViewerApp'
  'cs!views/MapViewerAppView'
  'cs!models/SearchApp'
  'cs!views/SearchAppView'
  'cs!views/MessageView'
  'cs!routers/LayersRouter'
  'cs!routers/SearchRouter'
], (
  _, $, Backbone, Bootstrap, StudyAreaView, MapViewerApp, MapViewerAppView, SearchApp, SearchAppView, MessageView, LayersRouter, SearchRouter,ChartView
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
    do @initSearch if $('#search').length
    do @initStudyAreaMap if $('#studyarea-map').length

    $('.chart').each (i, e) -> new ChartView el: e
    do Backbone.history.start

  ###
  Initialize the WMS map viewer
  ###
  initMapviewer: ->
    app    = new MapViewerApp()
    view   = new MapViewerAppView model: app
    router = new LayersRouter model: app

    @createMessageViewFor app

  ###
  Initialize the search
  ###
  initSearch: ->
    app    = new SearchApp()
    view   = new SearchAppView model: app
    router = new SearchRouter model: app, location: window.location

    @createMessageViewFor app

  ###
  Initialize the Study Area map
  ###
  initStudyAreaMap: ->
    view = new StudyAreaView
      el: '#studyarea-map'

  ###
  Create a message view. Which listens to the supplied app model for messages (errors, info)
  ###
  createMessageViewFor: (app) -> new MessageView model: app


