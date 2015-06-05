define [
  'jquery'
  'backbone'
  'cs!views/StudyAreaView'
  'cs!models/MapViewerApp'
  'cs!views/MapViewerAppView'
  'cs!models/SearchApp'
  'cs!views/SearchAppView'
  'cs!views/MessageView'
  'cs!routers/LayersRouter'
  'cs!routers/SearchRouter'
  'cs!models/GeminiMetadata'
  'cs!views/GeminiEditorView'
  'cs!models/PermissionApp'
  'cs!routers/PermissionRouter'
  'cs!views/PermissionAppView'
  'bootstrap'
], ($, Backbone, StudyAreaView, MapViewerApp, MapViewerAppView, SearchApp, SearchAppView, MessageView, LayersRouter,
    SearchRouter, GeminiMetadata, GeminiEditorView, PermissionApp, PermissionRouter, PermissionAppView) ->
  
  ###
  This is the initalizer method for the entire requirejs project. Here we can
  set up the different applications and initialize any javascript code which
  we like globally.
  ###
  initialize: ->
    do @initStudyAreaMap if $('#studyarea-map').length
    do @initMapviewer if $('#mapviewer').length
    do @initSearch if $('#search').length
    do @initEditor if $('.edit-control').length
    do @initPermission if $('.permission').length
      
  initStudyAreaMap: ->
    view = new StudyAreaView();
      
  ###
  Initialize the map viewer app, view and router
  ###
  initMapviewer: ->
    app    = new MapViewerApp()
    view   = new MapViewerAppView model: app
    router = new LayersRouter model: app

    @createMessageViewFor app
    try
      do Backbone.history.start
    catch ex
      console.log "history already started"

  ###
  Initialize the search application
  ###
  initSearch: ->
    app    = new SearchApp()
    view   = new SearchAppView model: app
    router = new SearchRouter model: app, location: window.location
    
    @createMessageViewFor app
    try
      do Backbone.history.start
    catch ex
      console.log "history already started"

  ###
  Initialize the editor application
  ###
  initEditor: ->

    $('.edit-control.gemini').on 'click', (event) ->
      model = null
      el = null

      do event.preventDefault

      if gemini?
        model = new GeminiMetadata gemini
        el = '#metadata'
      else
        model = new GeminiMetadata()
        el = '#search'

      view = new GeminiEditorView
        el: el
        model: model

  ###
  Initialize the permission application
  ###
  initPermission: ->
    app = new PermissionApp()
    view = new PermissionAppView model: app
    router = new PermissionRouter model: app

    @createMessageViewFor app
    try
      do Backbone.history.start
    catch ex
      console.log "history already started"

  ###
  Create a message view. Which listens to the supplied app model for messages (errors, info)
  ###
  createMessageViewFor: (app) -> new MessageView model: app