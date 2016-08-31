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
  'cs!models/EditorMetadata'
  'cs!views/GeminiEditorView'
  'cs!views/MonitoringEditorView'
  'cs!models/PermissionApp'
  'cs!routers/PermissionRouter'
  'cs!views/PermissionAppView'
  'cs!models/Catalogue'
  'cs!views/CatalogueView'
  'cs!views/ChartView'
  'cs!views/ModelEditorView'
  'cs!views/LinkEditorView'
  'cs!models/LinkEditorMetadata'
  'bootstrap'
], ($, Backbone, StudyAreaView, MapViewerApp, MapViewerAppView, SearchApp, SearchAppView, MessageView, LayersRouter,
    SearchRouter, EditorMetadata, GeminiEditorView, MonitoringEditorView, PermissionApp, PermissionRouter,
    PermissionAppView, Catalogue, CatalogueView, ChartView, ModelEditorView, LinkEditorView, LinkEditorMetadata) ->

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
    do Backbone.history.start

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

  ###
  Initialize the search application
  ###
  initSearch: ->
    app    = new SearchApp()
    view   = new SearchAppView model: app
    router = new SearchRouter model: app, location: window.location

    @createMessageViewFor app

  ###
  Initialize the editor application
  ###
  initEditor: ->

    lookup =
      GEMINI_DOCUMENT:
        View: GeminiEditorView
        Model: EditorMetadata
        mediaType: 'application/gemini+json'
      EF_DOCUMENT:
        View: MonitoringEditorView
        Model: EditorMetadata
        mediaType: 'application/monitoring+json'
      IMP_DOCUMENT:
        View: ModelEditorView
        Model: EditorMetadata
        mediaType: 'application/model+json'
      LINK_DOCUMENT:
        View: LinkEditorView
        Model: LinkEditorMetadata
        mediaType: 'application/link+json'

    # the create document dropdown
    $editorCreate = $ '#editorCreate'

    $('.edit-control').on 'click', (event) ->
      do event.preventDefault
      do $editorCreate.toggle

      documentType = lookup[$(event.target).data('documentType')]

      if $editorCreate.length
        new documentType.View
          model: new documentType.Model null, documentType
          el: '#search'
      else
        $.ajax
          url: $(location).attr('href')
          dataType: 'json'
          accepts:
            json: documentType.mediaType
          success: (data) ->
            new documentType.View
              model: new documentType.Model data, documentType
              el: '#metadata'

  ###
  Initialize the permission application
  ###
  initPermission: ->
    app = new PermissionApp()
    view = new PermissionAppView model: app
    router = new PermissionRouter model: app

    @createMessageViewFor app

  ###
  Initialize the catalogue application
  ###
  initCatalogue: ->
    $('button').on 'click', (event) ->
      do event.preventDefault
      $.getJSON $(location).attr('href'), (data) ->
        new CatalogueView
          el: '.catalogue'
          model: new Catalogue data

  ###
  Create a message view. Which listens to the supplied app model for messages (errors, info)
  ###
  createMessageViewFor: (app) -> new MessageView model: app
