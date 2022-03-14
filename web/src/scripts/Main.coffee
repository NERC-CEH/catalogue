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
  'cs!views/ServiceAgreementEditorView'
  'cs!models/service-agreement/ServiceAgreement'
], (
    _, $, Backbone, Bootstrap, StudyAreaView, MapViewerApp, MapViewerAppView, SearchApp, SearchAppView, MessageView, LayersRouter, SearchRouter, ServiceAgreementEditorView, ServiceAgreement, EditorMetadata
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

    do @initGeometryMap if $('#geometry-map').length
    do @initMapviewer if $('#mapviewer').length
    do @initSearch if $('#search').length
    do @initServiceAgreement if $('.service-agreement').length
    do @initStudyAreaMap if $('#studyarea-map').length

    $('.chart').each (i, e) -> new ChartView el: e
    do Backbone.history.start

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


