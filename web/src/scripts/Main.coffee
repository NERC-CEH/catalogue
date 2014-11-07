define [
  'jquery'
  'cs!views/StudyAreaView'
  'cs!models/MapViewerApp'
  'cs!views/MapViewerAppView'
  'cs!models/SearchApp'
  'cs!views/SearchAppView'
  'cs!views/ErrorMessageView'
  'cs!routers/LayersRouter'
  'cs!routers/SearchRouter'
  'bootstrap'
], ($, StudyAreaView, MapViewerApp, MapViewerAppView, SearchApp, SearchAppView, ErrorMessageView, LayersRouter, SearchRouter) ->
  
  ###
  This is the initalizer method for the entire requirejs project. Here we can
  set up the different applications and initialize any javascript code which
  we like globally.
  ###
  initialize: ->
    do @initStudyAreaMap if $('#studyarea-map').length
    do @initMapviewer if $('#mapviewer').length
    do @initSearch if $('#search').length
      
  initStudyAreaMap: ->
    view = new StudyAreaView();
      
  ###
  Initialize the map viewer app, view and router
  ###
  initMapviewer: ->
    app    = new MapViewerApp()
    view   = new MapViewerAppView model: app
    router = new LayersRouter model: app

    @createErrorMessageViewFor app
    do Backbone.history.start

  ###
  Initialize the search application
  ###
  initSearch: ->
    app    = new SearchApp()
    view   = new SearchAppView model: app
    router = new SearchRouter model: app, location: window.location
    
    @createErrorMessageViewFor app
    do Backbone.history.start

  ###
  Create a error message view. Which listens to the supplied app model for errors
  ###
  createErrorMessageViewFor: (app) -> new ErrorMessageView model: app