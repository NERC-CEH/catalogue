define [
  'jquery'
  'backbone'
  'cs!collections/upload/hubbub/FileCollection'
  'cs!models/upload/hubbub/File'
  'cs!views/upload/hubbub/FileView'
  'tpl!templates/upload/hubbub/FileRow.tpl'
  'cs!views/upload/hubbub/DropzoneView'
], ($, Backbone, FileCollection, File, FileView, fileRowTemplate, DropzoneView) -> Backbone.View.extend

  events:
    'click .finish': 'showFinish'
    'click .move-all': 'moveAllDatastore'
    'click .reschedule': 'reschedule'
    'click .schedule': 'schedule'
    'click .validate-all': 'validateAll'

  initialize: ->
    if @$('.dropzone-container').length
      dropzoneSuccess = (file) =>
        model = new File
          bytes: file.size
          name: file.name
          path: "/dropbox/#{@model.get('id')}/#{file.name}"
          status: 'WRITING'
        @addOne(@dropbox, @$dropbox, model)
        $(file.previewElement).remove()
#        TODO: query Hubbub for state of model after a delay to allow validating
        setTimeout(
          () -> console.log("updating #{model.get('path')}"),
          7000
        )

      new DropzoneView
        el: '.dropzone-container'
        success: dropzoneSuccess
        url: @model.url()

    @$datastore = @$('.datastore-files')
    @datastore = new FileCollection()
    @listenTo(@datastore, 'reset', (collection) => @addAll(collection, @$datastore))
    @listenTo(@datastore, 'add', (model) => @addOne(@datastore, @$datastore, model))

    @$dropbox = @$('.documents-files')
    @dropbox = new FileCollection()
    @listenTo(@dropbox, 'reset', (collection) => @addAll(collection, @$dropbox))
    @listenTo(@dropbox, 'add', (model) => @addOne(@dropbox, @$dropbox, model))

    @$metadata = @$('.metadata-files')
    @metadata = new FileCollection()
    @listenTo(@metadata, 'reset', (collection) => @addAll(collection, @$metadata))
    @listenTo(@metadata, 'add', (model) => @addOne(@metadata, @$metadata, model))

    $datastoreData = $('#datastore-data')
    @datastore.reset(JSON.parse(@$datastore.text())) if $datastoreData.length

    $dropboxData = $('#dropbox-data')
    @dropbox.reset(JSON.parse($dropboxData.text())) if $dropboxData.length

    $metadataData = $('#metadata-data')
    @metadata.reset(JSON.parse($metadataData.text())) if $metadataData.length

  addOne: (collection, $container, model) ->
    view = new FileView
      collection: collection
      model: model
      url: @model.url()
    $container.append(view.render().el)

  addAll: (collection, $container) ->
    $container.empty()
    collection.each((model) => @addOne(collection, $container, model))

  showModal: (title, body, action) ->
    $modal = $('#documentUploadModal')
    $('.modal-title', $modal).html(title)
    $('.modal-body', $modal).html(body)
    $('.modal-accept', $modal).unbind('click')
    $('.modal-accept', $modal).click(action.bind(@))
    $modal.modal('show')

  showInProgress: (event) ->
    $el = $(event.currentTarget)
    $el.attr('disabled', true)
    $icon = $('i', $el)
    current = $icon.attr('class')
    $icon.attr('class', 'btn-icon fas fa-sync fa-spin')
    console.log("show in progress")
    current

  showNormal: (event, classes) ->
    $el = $(event.currentTarget)
    $el.attr('disabled', false)
    $icon = $('i', $el)
    $icon.attr('class', classes)
    console.log("show normal")

  showInError: (event) ->
    $el = $(event.currentTarget)
    $el.attr('disabled', true)
    $icon = $('i', $el)
    $icon.attr('class', 'btn-icon fa fa-exclamation-triangle')
    console.log("show in error")

  showFinish: ->
    @showModal(
      'Have you finished uploading files?',
      'You will no longer be able to add, remove or update files.',
      @finish
    )

  finish: ->
    currentClasses = @showInProgress(event)
    $.ajax({
      url: "#{@model.url()}/finish"
      type: 'POST'
      success: =>
        @showNormal(event, currentClasses)
        window.location.assign("/documents/#{@model.get('id')}")
      error: (err) ->
        @showInError(event)
        console.error('error', err)
    })

  moveAllDatastore: ->
    currentClasses = @showInProgress(event)
    $.ajax({
      url: "#{@model.url()}/move-all-datastore"
      type: 'POST'
      success: =>
        @showNormal(event, currentClasses)
      error: (err) =>
        @showInError(event)
        console.error('error', err)
    })

  reschedule: ->
    currentClasses = @showInProgress(event)
    $.ajax({
      url: "#{@model.url()}/reschedule"
      type: 'POST'
      success: =>
        @showNormal(event, currentClasses)
#        TODO: re-render based on changed state of model rather than reloading
        window.location.reload()
      error: (err) =>
        @showInError(event)
        console.error('error', err)
    })

  schedule: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax({
      url: "#{@model.url()}/schedule"
      type: 'POST'
      success: =>
        @showNormal(event, currentClasses)
#        TODO: re-render based on changed state of model rather than reloading
        window.location.reload()
      error: (err) =>
        @showInError(event)
        console.error('error', err)
    })

  validateAll: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax({
      url: "#{@model.url()}/validate"
      type: 'POST'
      success: =>
        @showNormal(event, currentClasses)
      error: (err) =>
        @showInError(event)
        console.error('error', err)
    })

  pagination: (name, pageName) ->
#    TODO: fix pagination
    uploadFiles = @model.get('uploadFiles')
    pagination = uploadFiles[name].pagination
    page = pagination.page
    pages = Math.ceil(pagination.total / pagination.size)

    width = 38
    if page >= 10000
      width += 40
    else if page >= 1000
      width += 30
    else if page >= 100
      width += 20
    else if page >= 10
      width += 10

    $("#pag-#{name} .pag-current").css("width", width)
    $("#pag-#{name} .pag-current").val(page)
    $("#pag-#{name} .pag-current").unbind('input')
    $("#pag-#{name} .pag-current").on('input', (evt) =>
      value = Number(evt.target.value)
      if value > 0 and value <= pages
        @model.page[pageName] = value
        $("#pag-#{name} .pag-per-page .fa-spinner").css('visibility', 'visible')
        do @fetch
    )

    $("#pag-#{name} .pag-next").attr('disabled', page == pages)
    $("#pag-#{name} .pag-next").unbind('click')
    $("#pag-#{name} .pag-next").click(() =>
      @model.page[pageName] = @model.page[pageName] + 1
      @model.page[pageName] = pages if @model.page[pageName] >= pages

      $("#pag-#{name} .pag-current").val(@model.page[pageName])
      $("#pag-#{name} .pag-per-page .fa-spinner").css('visibility', 'visible')
      do @fetch
    )

    $("#pag-#{name} .pag-previous").attr('disabled', page == 1)
    $("#pag-#{name} .pag-previous").unbind('click')
    $("#pag-#{name} .pag-previous").click(() =>
      @model.page[pageName] = @model.page[pageName] - 1
      @model.page[pageName] = 1 if @model.page[pageName] <= 1

      $("#pag-#{name} .pag-current").val(@model.page[pageName])
      $("#pag-#{name} .pag-per-page .fa-spinner").css('visibility', 'visible')
      do @fetch
    )

    $("#pag-#{name} .pag-count").text("/ #{pages}")

  renderPagination: () ->
    @pagination('documents', 'documentsPage')
    @pagination('supporting-documents', 'supportingDocumentsPage')
    @pagination('datastore', 'datastorePage')

  render: ->
    do @renderPagination
