define [
  'jquery'
  'backbone'
  'filesize'
  'tpl!templates/DocumentUploadFileRow.tpl'
  'tpl!templates/BetaDropzoneFile.tpl'
], (
  $
  Backbone
  filesize
  DocumentUploadFileRowTemplate
  DropzoneFileTpl
) -> Backbone.View.extend
  dropzone: null

  initialize: ->
    setInterval(
      () => do @model.fetch
      1000
    )

    @model.on 'sync', =>
      do @render
      do @initDropzone if @dropzone == null && $('.dropzone-container').length != 0
      do $('.loading').remove
      $('.messages').hide 'fast'
    do @model.fetch

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

  globalAction: (name, action) ->
    action = action || name
    $(".#{name}").unbind('click')
    $(".#{name}").click((evt) => @model[action]())

  renderChecksums: ->
    checksums = []

    files = @model.get('uploadFiles').datastore.documents || {}
    files = (value for own prop, value of files)
    files.sort (left, right) ->
      return -1 if left.name < right.name
      return 1 if left.name > right.name
      return 0
    for index, file of files
      checksums.push file.name + ',' + file.hash

    files = @model.get('uploadFiles').datastore.invalid || {}
    files = (value for own prop, value of files)
    files.sort (left, right) ->
      return -1 if left.name < right.name
      return 1 if left.name > right.name
      return 0
    for index, file of files
      checksums.push file.name + ',' + file.hash

    href = 'data:text/csv;charset=utf-8,' + encodeURI(checksums.join('\n\r'))

    $('.downloadChecksum').attr('href', href)

  initDropzone: (url) ->
    model = @model.bind @
    render = @render.bind @

    options =
      timeout: -1
      url: window.location.origin + model.url() + '/add-upload-document'
      maxFilesize: 20 * 1000 * 1000
      autoQueue: yes
      previewTemplate: DropzoneFileTpl()
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
        
        @on 'success', (file, res) ->
          id = file.name.replace(/[^\w?]/g, '-')
          setTimeout(
            -> $('.uploading-' + id).remove()
            500
          )
          model.set res
          do render
        
        @on 'error', (file, errorMessage, xhr) ->
          id = file.name.replace(/[^\w?]/g, '-')
          $('.uploading-' + id + ' .file-status').text('Error')
          errorMessages =
            0: 'No connection'
            403: 'Unauthorized'
            500: 'Internal Server Error'
          errorMessage = errorMessage
          errorMessage = errorMessages[xhr.status] || errorMessage if xhr
          $('.uploading-' + id + ' .file-message').text(errorMessage)

    # @dropzone = $('.dropzone-container').dropzone(options)
    @dropzone = new Dropzone('.dropzone-container', options)
    

  renderZip: ->
    if @model.get('uploadFiles').datastore && @model.get('uploadFiles').datastore.zipped
      $('.datastore-icon .far').removeClass('fa-file')
      $('.datastore-icon .far').addClass('fa-file-archive')
      do $('.zip').hide
      do $('.unzip').show
    else
      $('.datastore-icon .far').removeClass('fa-file-archive')
      $('.datastore-icon .far').addClass('fa-file')
      do $('.zip').show
      do $('.unzip').hide

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
    M = date.getMonth()
    y = ('' + date.getFullYear()).slice(2)
    
    h = date.getHours()
    h = "0#{h}" if h < 10

    m = date.getMinutes()
    m = "0#{m}" if m < 10

    "#{d}/#{M}/#{y} - #{h}:#{m}"

  renderFiles: ->
    uploadFiles = @model.get('uploadFiles')
    for name of uploadFiles
        filesEl = $(".#{name}-files")
        filesEl.html('')
        if typeof uploadFiles[name].documents == 'undefined' && typeof uploadFiles[name].invalid == 'undefined'
            filesEl.append($("<h3 class='no-documents'>NO FILES IN #{@model.keyToName[name].toUpperCase()}</h3>"))
        for filename, data of uploadFiles[name].invalid
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
            row = $(DocumentUploadFileRowTemplate data)
            filesEl.append(row)
        for filename, data of uploadFiles[name].documents
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
            row = $(DocumentUploadFileRowTemplate data)
            filesEl.append(row)

        @fileAction('accept')
        @fileAction('delete')
        @fileAction('validate')
        @fileAction('ignore')
        @fileAction('cancel')
        @fileAction('move-metadata', 'move', 'supporting-documents')
        @fileAction('move-datastore', 'move', 'eidchub')

        $('.panel-heading').unbind('click')
        $('.panel-heading').click(
          (evt) =>
            el = $(evt.target)
            if evt.target.className != 'panel-heading'
                el = $(evt.target).parent()
            panel = el.parent()
            panel.toggleClass('is-collapsed')
            filename = panel.data('filename')
            @model.open[filename] = !panel.hasClass('is-collapsed')
        )

  render: ->
    @globalAction 'move-all', 'moveToDatastore'
    @globalAction 'validate-all', 'validateFiles'
    @globalAction 'zip'
    @globalAction 'unzip'
    @globalAction 'finish'

    do @renderChecksums
    do @renderZip
    do @renderFiles
