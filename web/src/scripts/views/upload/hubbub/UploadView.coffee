define [
  'underscore'
  'jquery'
  'backbone'
  'dropzone'
  'filesize'
  'tpl!templates/upload/hubbub/FileRow.tpl'
  'tpl!templates/upload/hubbub/DropzoneFileRow.tpl'
], (_, $, Backbone, Dropzone, filesize, fileRowTemplate, dropzoneFileTpl
) -> Backbone.View.extend

  pollingTimeout: null
  fetchXhr: null

  events:
    'click .finish': 'showFinish'
    'click .move-all': 'moveAllDatastore'
    'click .reschedule': 'reschedule'
    'click .schedule': 'schedule'
    'click .validate-all': 'validateAll'

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
    $.ajax({
      url: "#{@model.url()}/finish"
      type: 'POST'
      success: =>
        window.location.assign("/documents/#{@model.id}")
      error: (err) ->
        console.error('error', err)
    })

  moveAllDatastore: ->
    $.ajax({
      url: "#{@model.url()}/move-all-datastore"
      type: 'POST'
      success: ->
        console.log("Moved to datastore")
      error: (err) ->
        console.error('error', err)
    })

  reschedule: ->
    $.ajax({
      url: "#{@model.url()}/reschedule"
      type: 'POST'
      success: ->
        window.location.reload()
      error: (err) ->
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
      error: (err) ->
        console.error('error', err)
    })

  validateAll: (event) ->
    console.log(event)
    #TODO: why does this get 405 "Method Not Allowed" error?
    @showInProgress(event)
    $.ajax({
      url: "#{@model.url()}/validate"
      success: ->
        console.log("Validate all")
      error: (err) =>
        @showInError(event)
        console.error('error', err)
    })

  initialize: ->
    @fetch = () =>
      clearTimeout(@pollingTimeout)

      if @fetchXhr != null && @fetchXhr.readyState > 0 && @fetchXhr.readyState < 4
        @fetchXhr.abort()

      @fetchXhr = @model.fetch
        data:
          documents_page: @model.page.documentsPage
          datastore_page: @model.page.datastorePage
          supporting_documents_page: @model.page.supportingDocumentsPage

    do @initDropzone if @$('.dropzone-container').length

    @model.on 'sync', =>
      do @render
      do $('.loading').remove
      $('.messages').hide 'fast'
      $('.pag-per-page .fa-spinner').css('visibility', 'hidden')
      @pollingTimeout = setTimeout(@fetch, 7000)

    @model.on 'change', => do @render

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

  initDropzone: ->
    options =
      timeout: -1
      url: @model.url()
      maxFilesize: 20 * 1000 * 1000
      autoQueue: yes
      previewTemplate: dropzoneFileTpl()
      previewsContainer: '.dropzone-files'
      clickable: '.fileinput-button'
      parallelUploads: 1
    init: ->
      @on 'uploadprogress', (file) ->
        id = file.name.replace(/[^\w?]/g, '-')
        $(".uploading-#{id} .file-status").text("Uploaded #{filesize(file.upload.bytesSent)}")
        if file.upload.progress == 100
          $(".uploading-#{id} .file-status").text('Writing to Disk')
          $(".uploading-#{id} .cancel").attr('disabled', true)

      @on 'addedfile', (file) ->
        last = $('.uploading').length - 1
        uploading = $($('.uploading')[last])
        id = file.name.replace(/[^\w?]/g, '-')
        uploading.addClass('uploading-' + id)
        uploading.find('.cancel').click => @removeFile file
        $(".uploading-#{id} .file-size-value").text("#{filesize(file.size)}")

      @on 'success', (file) ->
        id = file.name.replace(/[^\w?]/g, '-')
        setTimeout(
          -> $('.uploading-' + id).remove()
          500
        )

      @on 'error', (file, errorMessage, xhr) ->
        id = file.name.replace(/[^\w?]/g, '-')
        $('.uploading-' + id + ' .file-status').text('Error')
        errorMessages =
          0: 'No connection'
          403: 'Unauthorized'
          500: 'Internal Server Error'
        errorMessage = errorMessages[xhr.status] || errorMessage if xhr
        $('.uploading-' + id + ' .file-message').text(errorMessage)

    @dropzone = new Dropzone('.dropzone-container', options)


  sizeToTime: (size) ->
    time = ''
    for key, value of @model.timeEstimate
      if size < key
        time = value
      break
    time

  simpleDate: (time) ->
    date = new Date(time)
    d = date.getDate()
    M = date.getMonth() + 1
    y = ('' + date.getFullYear()).slice(2)

    h = date.getHours()
    h = "0#{h}" if h < 10

    m = date.getMinutes()
    m = "0#{m}" if m < 10

    "#{d}/#{M}/#{y} - #{h}:#{m}"

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
      filesEl = $(".#{name}-files")
      filesEl.html('')
      if typeof uploadFiles[name].documents == 'undefined' && typeof uploadFiles[name].invalid == 'undefined'
        filesEl.append($("<h3 class='no-documents'>NO FILES IN #{@model.keyToName[name].toUpperCase()}</h3>"))
      for filename in Object.keys(uploadFiles[name].invalid || {}).sort()
        data = uploadFiles[name].invalid[filename]
        @model.open[filename] = true if typeof @model.open[filename] == 'undefined'
        data.moving = false
        data.errorType = @model.errorType[data.type] || ''
        data.hash = data.hash || 'NO HASH'
        data.action = @model.errorActions[data.type]
        data.el = "#{name}-#{data.id}"
        data.size = filesize(data.bytes)
        data.estimate = @sizeToTime(data.bytes)
        data.message = @model.messages[data.type]
        data.date = @simpleDate(data.time)
        data.open = @model.open[filename]
        row = $(fileRowTemplate,  data)
        filesEl.append(row)
      for filename in Object.keys(uploadFiles[name].documents || {}).sort()
        data = uploadFiles[name].documents[filename]
        @model.open[filename] = false if typeof @model.open[filename] == 'undefined'
        data.errorType = 'valid'
        data.moving = data.type.includes('MOVING') || data.type == 'WRITING'
        data.validating = data.type == 'VALIDATING_HASH'
        data.hash = data.hash || 'NO HASH'
        data.action = @model.keyToAction[name]
        data.el = "#{name}-#{data.id}"
        data.size = filesize(data.bytes)
        data.estimate = @sizeToTime(data.bytes)
        data.message = @model.messages[data.type]
        data.date = @simpleDate(data.time)
        data.open = @model.open[filename]
        row = $(fileRowTemplate,  data)
        filesEl.append(row)

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
    do @renderFiles
    do @renderPagination
