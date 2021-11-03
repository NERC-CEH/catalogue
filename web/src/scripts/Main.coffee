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
  'cs!views/OsdpAgentEditorView'
  'cs!views/OsdpDatasetEditorView'
  'cs!views/OsdpModelEditorView'
  'cs!views/OsdpSampleEditorView'
  'cs!views/OsdpPublicationEditorView'
  'cs!views/OsdpMonitoringActivityEditorView'
  'cs!views/OsdpMonitoringProgrammeEditorView'
  'cs!views/OsdpMonitoringFacilityEditorView'
  'cs!views/SampleArchiveEditorView'
  'cs!views/ErammpModelEditorView'
  'cs!views/NercModelEditorView'
  'cs!views/NercModelUseEditorView'
  'cs!views/ErammpDatacubeEditorView'
  'cs!views/UkemsDocumentEditorView'
  'cs!views/DatalabsDocumentEditorView'
  'cs!views/ClipboardCopyView'
  'cs!views/DataTypeEditorView'
  'cs!views/ElterEditorView'
  'cs!views/ElterLinkedEditorView'
  'cs!views/upload/simple/AppView'
  'cs!views/ServiceAgreementEditorView'
  'cs!models/ServiceAgreementEditorMetadata'
], (
    _, $, Backbone, Bootstrap, StudyAreaView, MapViewerApp, MapViewerAppView, SearchApp, SearchAppView, MessageView, LayersRouter, SearchRouter,
    EditorMetadata, GeminiEditorView, MonitoringEditorView, PermissionApp, PermissionRouter, PermissionAppView, Catalogue, CatalogueView,
    ChartView, ModelEditorView, LinkEditorView, LinkEditorMetadata, CehModelEditorView, CehModelApplicationEditorView, OsdpAgentEditorView,
    OsdpDatasetEditorView, OsdpModelEditorView, OsdpSampleEditorView, OsdpPublicationEditorView, OsdpMonitoringActivityEditorView, OsdpMonitoringProgrammeEditorView,
    OsdpMonitoringFacilityEditorView, SampleArchiveEditorView, ErammpModelEditorView, NercModelEditorView, NercModelUseEditorView, ErammpDatacubeEditorView, UkemsDocumentEditorView,
    DatalabsDocumentEditorView, ClipboardCopyView, DataTypeEditorView, ElterEditorView, ElterLinkedEditorView, SimpleUploadView, ServiceAgreementEditorView, ServiceAgreementEditorMetadata
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

    do @initCatalogue if $('.catalogue-control').length
    do @initClipboard if $('.clipboard-copy').length
    do @initEditor if $('.edit-control').length
    do @initGeometryMap if $('#geometry-map').length
    do @initMapviewer if $('#mapviewer').length
    do @initPermission if $('.permission').length
    do @initSearch if $('#search').length
    do @initServiceAgreement if $('.service-agreement').length
    do @initSimpleUpload if $('#simple-upload').length
    do @initStudyAreaMap if $('#studyarea-map').length

    $('.chart').each (i, e) -> new ChartView el: e
    do Backbone.history.start

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
  Initialize clipboard copy
  ###
  initClipboard: ->
    view = new ClipboardCopyView
      el: '.clipboard-copy'

  ###
  Initialize the editor
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
      'erammp-model':
        View: ErammpModelEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.erammp-model+json'
      'nerc-model':
        View: NercModelEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.nerc-model+json'
      'nerc-model-use':
        View: NercModelUseEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.nerc-model-use+json'
      'erammp-datacube':
        View: ErammpDatacubeEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.erammp-datacube+json'
      'data-type':
        View: DataTypeEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.data-type+json'
      'elter':
        View: ElterEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.elter+json'
      'linked-elter':
        View: ElterLinkedEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.linked-elter+json'
      'service-agreement':
        View: ServiceAgreementEditorView
        Model: ServiceAgreementEditorMetadata
        mediaType: 'application/json'
      'ukems-document':
        View: UkemsDocumentEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.ukems-document+json'
      'datalabs-document':
        View: DatalabsDocumentEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.datalabs-document+json'

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
  Initialize the geometry map
  ###
  initGeometryMap: ->
    view = new StudyAreaView
      el: '#geometry-map'

  ###
  Initialize the WMS map viewer
  ###
  initMapviewer: ->
    app    = new MapViewerApp()
    view   = new MapViewerAppView model: app
    router = new LayersRouter model: app

    @createMessageViewFor app

  ###
  Initialize the permission application
  ###
  initPermission: ->
    app = new PermissionApp()
    view = new PermissionAppView model: app
    router = new PermissionRouter model: app

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
  Initialize the Service Agreement editor
  ###
  initServiceAgreement: ->

    $gemini = $ '#service-agreement-gemini'

    $('.service-agreement').on 'click', (event) ->

      do event.preventDefault
      id =  $(event.currentTarget).data("id")
      data = eidcContactDetails: 'info@eidc.ac.uk'
      options = id: $(event.currentTarget).data("id")

      if $gemini.length
        $.ajax
          url: "/service-agreement/#{id}"
          type: 'GET'
          success: ->
            window.location.href = "/service-agreement/#{id}"
          error: ->
            new ServiceAgreementEditorView
              el: '#metadata'
              model: new ServiceAgreementEditorMetadata(data, options)

  ###
  Initialize the simple dataset upload
  ###
  initSimpleUpload: ->
    url = $('#simple-upload-dropzone').attr('action')
    view = new SimpleUploadView
      el: '#simple-upload'
      url: url

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


