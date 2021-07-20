define [
  'jquery'
  'backbone'
  'dropzone'
  'filesize'
  'cs!collections/upload/hubbub/FileCollection'
  'cs!models/upload/hubbub/File'
  'cs!views/upload/hubbub/FileView'
  'tpl!templates/upload/hubbub/FileRow.tpl'
  'tpl!templates/upload/hubbub/DropzoneFileRow.tpl'
], ($, Backbone, Dropzone, filesize, FileCollection, File, FileView, fileRowTemplate, dropzoneFileTpl) -> Backbone.View.extend

  events:
    'click .finish': 'showFinish'
    'click .move-all': 'moveAllDatastore'
    'click .reschedule': 'reschedule'
    'click .schedule': 'schedule'
    'click .validate-all': 'validateAll'

  initialize: ->
    new Dropzone('.dropzone-container', @dropzoneOptions()) if @$('.dropzone-container').length

    $datastore = @$('.datastore-files')
    datastore = new FileCollection()
    @listenTo(datastore, 'reset', (collection) => @addAll(collection, $datastore))

    $dropbox = @$('.documents-files')
    dropbox = new FileCollection()
    @listenTo(dropbox, 'reset', (collection) => @addAll(collection, $dropbox))

    $metadata = @$('.metadata-files')
    metadata = new FileCollection()
    @listenTo(metadata, 'reset', (collection) => @addAll(collection, $metadata))

    $datastoreData = $('#datastore-data')
#    TODO: remove NO FILES message
    datastore.reset(JSON.parse($datastore.text())) if $datastoreData.length

    $dropboxData = $('#dropbox-data')
    dropbox.reset(JSON.parse($dropboxData.text())) if $dropboxData.length

    $metadataData = $('#metadata-data')
    metadata.reset(JSON.parse($metadataData.text())) if $metadataData.length




    @model.on 'sync', =>
      do @render
      do $('.loading').remove
      $('.messages').hide 'fast'
      $('.pag-per-page .fa-spinner').css('visibility', 'hidden')
      @pollingTimeout = setTimeout(@fetch, 7000)

    @model.on 'change', => do @render

  addOne: ($container, model) ->
    view = new FileView({model: model})
    $container.append(view.render().el)

  addAll: (collection, $container) ->
    console.log(collection)
    console.log($container)
    collection.each((model) => @addOne($container, model))

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
        window.location.assign("/documents/#{@model.id}")
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
        console.log("Moved to datastore")
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
        window.location.reload()
      error: (err) =>
        @showInError(event)
        console.error('error', err)
    })

  validateAll: (event) ->
    #TODO: why does this get 405 "Method Not Allowed" error?
    currentClasses = @showInProgress(event)
    $.ajax({
      url: "#{@model.url()}/validate"
      success: =>
        @showNormal(event, currentClasses)
        console.log("Validate all")
      error: (err) =>
        @showInError(event)
        console.error('error', err)
    })

  fileAction: (name, action, to) ->
    action = action || name
    $(".#{name}").unbind('click')
    $(".#{name}").click((evt) =>
        el = $(evt.target)
        filename = el.data('filename')
        if typeof filename == 'undefined'
            el = $(evt.target).parent()
        filename = el.data('filename')
        el.children('i').attr('class', 'fas fa-sync fa-spin')
        el.attr('disabled', true)
        @model[action](filename, to)
    )

  dropzoneOptions: ->
    timeout: -1
    url: @model.url()
    maxFilesize: 20 * 1000 * 1000
    autoQueue: yes
    previewTemplate: dropzoneFileTpl()
    previewsContainer: '.dropzone-files'
    clickable: '.fileinput-button'
    parallelUploads: 1
    init: ->
      @on 'addedfile', (file) ->
        $file = $(file.previewElement)
        $file.find('.cancel').click => @removeFile(file)
        $file.find('.file-size-value').text("#{filesize(file.size)}")

      @on 'uploadprogress', (file, progress, bytesSent) ->
        $file = $(file.previewElement)
        if progress < 100
          $file.find('.file-status').text("Uploaded #{filesize(bytesSent)}")
        else
          $file.find('.file-status').text('Writing to Disk')
          $file.find('.cancel').attr('disabled', true)

      @on 'success', (file) ->
        $(file.previewElement).remove()

      @on 'error', (file, error, xhr) ->
        $file = $(file.previewElement)
        $file.find('.file-status').text('Error')
        $file.find('.file-name i').attr('class', 'fa fa-exclamation-triangle')
        $file.find('.cancel').attr('disabled', false)
        errorMessages =
          0: 'No connection'
          403: 'Unauthorized'
          500: 'Internal Server Error'
        message = errorMessages[xhr.status] || error.error if xhr
        $file.find('.file-message').text(message)

  pagination: (name, pageName) ->
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

  renderFiles: ->
    uploadFiles = @model.get('uploadFiles')
    for name of uploadFiles
      for filename in Object.keys(uploadFiles[name].invalid || {}).sort()
        data = uploadFiles[name].invalid[filename]
        data.action = @model.errorActions[data.type]

      for filename in Object.keys(uploadFiles[name].documents || {}).sort()
        data = uploadFiles[name].documents[filename]
        @model.open[filename] = false if typeof @model.open[filename] == 'undefined'
        data.action = @model.keyToAction[name]

      @fileAction('accept')
      @fileAction('delete', 'showDelete')
      @fileAction('validate')
      @fileAction('ignore', 'showIgnore')
      @fileAction('cancel', 'showCancel')
      @fileAction('move-metadata', 'move', 'metadata')
      @fileAction('move-datastore', 'move', 'datastore')

      $('.panel-heading').unbind('click')
      $('.panel-heading').click((evt) =>
        el = $(evt.target)
        if evt.target.className != 'panel-heading'
          el = $(evt.target).parent()
        panel = el.parent()
        panel.toggleClass('is-collapsed')
        filename = panel.data('filename')
        @model.open[filename] = !panel.hasClass('is-collapsed')
      )


  render: ->
    do @renderPagination
