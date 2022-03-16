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
  'cs!views/ChartView'
  'cs!views/CehModelEditorView'
  'cs!views/OsdpDatasetEditorView'
  'cs!views/OsdpMonitoringActivityEditorView'
  'cs!views/OsdpMonitoringProgrammeEditorView'
  'cs!views/OsdpMonitoringFacilityEditorView'
  'cs!views/SampleArchiveEditorView'
  'cs!views/ErammpModelEditorView'
  'cs!views/NercModelEditorView'
  'cs!views/ErammpDatacubeEditorView'
  'cs!views/RiRecordEditorView'
  'cs!views/ElterEditorView'
  'cs!views/ServiceAgreementEditorView'
  'cs!models/service-agreement/ServiceAgreement'
], (
  _, $, Backbone, Bootstrap, StudyAreaView, MapViewerApp, MapViewerAppView, SearchApp, SearchAppView, MessageView, LayersRouter, SearchRouter,
  EditorMetadata, GeminiEditorView, ChartView, CehModelEditorView, OsdpDatasetEditorView, OsdpMonitoringActivityEditorView, OsdpMonitoringProgrammeEditorView,
  OsdpMonitoringFacilityEditorView, SampleArchiveEditorView, ErammpModelEditorView, NercModelEditorView, ErammpDatacubeEditorView, RiRecordEditorView, ElterEditorView, ServiceAgreementEditorView, ServiceAgreement
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

    do @initEditor if $('.edit-control').length
    do @initGeometryMap if $('#geometry-map').length
    do @initMapviewer if $('#mapviewer').length
    do @initSearch if $('#search').length
    do @initServiceAgreement if $('.service-agreement').length
    do @initStudyAreaMap if $('#studyarea-map').length

    $('.chart').each (i, e) -> new ChartView el: e
    do Backbone.history.start

  ###
  Initialize the editor
  ###
  initEditor: ->

    lookup =
      GEMINI_DOCUMENT:
       View: GeminiEditorView
       Model: EditorMetadata
       mediaType: 'application/gemini+json'
      CEH_MODEL:
        View: CehModelEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.ceh.model+json'
      'osdp-dataset':
        View: OsdpDatasetEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.osdp.dataset+json'
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
      'erammp-datacube':
        View: ErammpDatacubeEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.erammp-datacube+json'
      'rirecord':
        View: RiRecordEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.rirecord+json'
      'elter':
        View: ElterEditorView
        Model: EditorMetadata
        mediaType: 'application/vnd.elter+json'
      'service-agreement':
        View: ServiceAgreementEditorView
        Model: ServiceAgreement
        mediaType: 'application/json'

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

    $gemini = $('#service-agreement-gemini')

    $('.service-agreement').on 'click', (event) ->

      do event.preventDefault
      id =  $(event.currentTarget).data("id")
      data = eidcContactDetails: 'info@eidc.ac.uk'
      options = id: id

      if $gemini.length
        $.ajax
          url: "/service-agreement/#{id}"
          type: 'GET'
          success: ->
            window.location.href = "/service-agreement/#{id}"
          error: ->
            new ServiceAgreementEditorView
              el: '#metadata'
              model: new ServiceAgreement(data, options)

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


