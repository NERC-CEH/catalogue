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
  'cs!models/MonitoringMetadata'
  'cs!views/MonitoringEditorView'
  'cs!models/PermissionApp'
  'cs!routers/PermissionRouter'
  'cs!views/PermissionAppView'
  'cs!models/Catalogue'
  'cs!views/CatalogueView'
  'cs!views/ChartView'
  'bootstrap'
], ($, Backbone, StudyAreaView, MapViewerApp, MapViewerAppView, SearchApp, SearchAppView, MessageView, LayersRouter,
    SearchRouter, GeminiMetadata, GeminiEditorView, MonitoringMetadata, MonitoringEditorView, PermissionApp, PermissionRouter,
    PermissionAppView, Catalogue, CatalogueView, ChartView) ->

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
    do @initCatalogue if $('.catalogue').length

    $('.chart').each (i, e) -> new ChartView el: e

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

    bindEditorView = (event, model, el, View) ->
      do event.preventDefault
      new View
        el: el
        model: model

      $('#editorCreate').toggle()

    $('.edit-control.gemini').on 'click', (event) ->
      model = el = null

      if gemini?
        model = new GeminiMetadata gemini
        el = '#metadata'
      else
        model = new GeminiMetadata()
        el = '#search'

      bindEditorView event, model, el, GeminiEditorView

    $('.edit-control.monitoring').on('click', (event) ->
      model = el = null

      if monitoring?
        model = new MonitoringMetadata monitoring
        el = '#metadata'
      else
        model = new MonitoringMetadata()
        el = '#search'

      bindEditorView event, model, el, MonitoringEditorView
    )

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
  Initialize the catalogue application
  ###
  initCatalogue: ->
    $('button').on 'click', (event) ->
      do event.preventDefault
      $.getJSON $(location).attr('href'), (data) ->
        model = new Catalogue data
        new CatalogueView
          el: '.catalogue'
          model: model

  ###
  Create a message view. Which listens to the supplied app model for messages (errors, info)
  ###
  createMessageViewFor: (app) -> new MessageView model: app
