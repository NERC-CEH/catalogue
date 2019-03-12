define [
  'underscore'
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
  'cs!views/CehModelEditorView'
  'cs!views/CehModelApplicationEditorView'
  'cs!views/DocumentsUploadScheduledView'
  'cs!models/DocumentsUploadScheduledModel'
  'cs!views/DocumentsUploadInProgressView'
  'cs!models/DocumentsUploadInProgressModel'
  'cs!views/DocumentsUploadReadOnlyView'
  'cs!models/DocumentsUploadReadOnlyModel'
  'cs!views/OsdpAgentEditorView'
  'cs!views/OsdpDatasetEditorView'
  'cs!views/OsdpModelEditorView'
  'cs!views/OsdpSampleEditorView'
  'cs!views/OsdpPublicationEditorView'
  'cs!views/OsdpMonitoringActivityEditorView'
  'cs!views/OsdpMonitoringProgrammeEditorView'
  'cs!views/OsdpMonitoringFacilityEditorView'
  'cs!views/SampleArchiveEditorView'
  'cs!models/DepositRequestModel'
  'cs!views/DepositRequestView'
  'cs!views/ClipboardCopyView'
  'cs!views/DataTypeEditorView'
  'bootstrap'
  'dropzone'
], (
  _, $, Backbone, StudyAreaView, MapViewerApp, MapViewerAppView, SearchApp, SearchAppView, MessageView, LayersRouter, SearchRouter,
    EditorMetadata, GeminiEditorView, MonitoringEditorView, PermissionApp, PermissionRouter, PermissionAppView, Catalogue, CatalogueView,
    ChartView, ModelEditorView, LinkEditorView, LinkEditorMetadata, CehModelEditorView, CehModelApplicationEditorView, DocumentsUploadScheduledView,
    DocumentsUploadScheduledModel, DocumentsUploadInProgressView, DocumentsUploadInProgressModel, DocumentsUploadReadOnlyView, DocumentsUploadReadOnlyModel, OsdpAgentEditorView,
    OsdpDatasetEditorView, OsdpModelEditorView, OsdpSampleEditorView, OsdpPublicationEditorView, OsdpMonitoringActivityEditorView, OsdpMonitoringProgrammeEditorView,
    OsdpMonitoringFacilityEditorView, SampleArchiveEditorView, DepositRequestModel, DepositRequestView,
    ClipboardCopyView, DataTypeEditorView
) ->

  ###
  This is the initalizer method for the entire requirejs project. Here we can
  set up the different applications and initialize any javascript code which
  we like globally.
  ###
  initialize: ->
    # shim
    # http://stackoverflow.com/a/646643
    String::startsWith ?= (s) -> @slice(0, s.length) == s
    String::endsWith   ?= (s) -> s == '' or @slice(-s.length) == s

    do @initScheduled if $('#documents-upload .scheduled').length
    do @initInProgress if $('#documents-upload .in-progress').length
    do @initReadOnly if $('#documents-upload .read-only').length
    do @initDepositRequest if $('#deposit-request').length
    do @initStudyAreaMap if $('#studyarea-map').length
    do @initGeometryMap if $('#geometry-map').length
    do @initMapviewer if $('#mapviewer').length
    do @initSearch if $('#search').length
    do @initEditor if $('.edit-control').length
    do @initPermission if $('.permission').length
    do @initCatalogue if $('.catalogue-control').length
    do @initClipboard if $('.clipboard-copy').length

    $('.chart').each (i, e) -> new ChartView el: e
    do Backbone.history.start

  initDepositRequest: ->
    app = new DepositRequestModel
    view = new DepositRequestView model: app

  initReadOnly: ->
    id = $('#documents-upload').data('guid')
    app = new DocumentsUploadReadOnlyModel null,
        mediaType: 'application/vnd.upload-document+json'
    app.id = id
    app.set('id', id)
    view = new DocumentsUploadReadOnlyView model: app

  initScheduled: ->
    id = $('#documents-upload').data('guid')
    app = new DocumentsUploadScheduledModel null,
        mediaType: 'application/vnd.upload-document+json'
    app.id = id
    app.set('id', id)
    view = new DocumentsUploadScheduledView model: app

  initInProgress: ->
    id = $('#documents-upload').data('guid')
    app = new DocumentsUploadInProgressModel null,
        mediaType: 'application/vnd.upload-document+json'
    app.id = id
    app.set('id', id)
    view = new DocumentsUploadInProgressView model: app

  initStudyAreaMap: ->
    view = new StudyAreaView
      el: '#studyarea-map'

  initGeometryMap: ->
    view = new StudyAreaView
      el: '#geometry-map'

  initClipboard: ->
    view = new ClipboardCopyView
      el: '.clipboard-copy'

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
      CEH_MODEL:
        View: CehModelEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.ceh.model+json'
      CEH_MODEL_APPLICATION:
        View: CehModelApplicationEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.ceh.model.application+json'
      LINK_DOCUMENT:
        View: LinkEditorView
        Model: LinkEditorMetadata
        mediaType: 'application/link+json'
      'osdp-agent':
        View: OsdpAgentEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.osdp.agent+json'
      'osdp-dataset':
        View: OsdpDatasetEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.osdp.dataset+json'
      'osdp-model':
        View: OsdpModelEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.osdp.model+json'
      'osdp-sample':
        View: OsdpSampleEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.osdp.sample+json'
      'osdp-publication':
        View: OsdpPublicationEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.osdp.publication+json'
      'osdp-monitoring-activity':
        View: OsdpMonitoringActivityEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.osdp.monitoring-activity+json'
      'osdp-monitoring-programme':
        View: OsdpMonitoringProgrammeEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.osdp.monitoring-programme+json'
      'osdp-monitoring-facility':
        View: OsdpMonitoringFacilityEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.osdp.monitoring-facility+json'
      'sample-archive':
        View: SampleArchiveEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.sample-archive+json'
      'data-type':
        View: DataTypeEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.data-type+json'

    # the create document dropdown
    $editorCreate = $ '#editorCreate'

    $('.edit-control').on 'click', (event) ->
      do event.preventDefault

      title = $(event.target).data('documentType')
      documentType = lookup[title]

      if $editorCreate.length
        new documentType.View
          model: new documentType.Model null, documentType, title
          el: '#search'
      else
        $.ajax
          url: $(location).attr('href')
          dataType: 'json'
          accepts:
            json: documentType.mediaType
          success: (data) ->
            new documentType.View
              model: new documentType.Model data, documentType, title
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
    catalogues = undefined

    $.getJSON '/catalogues', (data) ->
      catalogues = _.chain(data).map((c) -> { value: c.id, label: c.title }).value()

    $('.catalogue-control').on 'click', (event) ->
      do event.preventDefault
      $.getJSON $(event.target).attr('href'), (data) ->
        model = new Catalogue data
        model.options = catalogues

        new CatalogueView
          el: '#metadata'
          model: model

  ###
  Create a message view. Which listens to the supplied app model for messages (errors, info)
  ###
  createMessageViewFor: (app) -> new MessageView model: app
