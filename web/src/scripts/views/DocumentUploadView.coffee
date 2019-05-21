define [
  'jquery'
  'backbone'
  'tpl!templates/DocumentUploadFileRow.tpl'
  'tpl!templates/BetaDropzoneFile.tpl'
], ($, Backbone, DocumentUploadFileRowTemplate, DropzoneFileTpl) -> Backbone.View.extend
  dropzone: null

  keyToName:
    documents: 'data'
    plone: 'metadata'
    datastore: 'datastore'
  
  keyToAction:
    documents: 'move-both'
    plone: 'move-datastore'
    datastore: 'move-metadata'

  messages:
    CHANGED_HASH: 'The file has changed'
    NO_HASH: 'This file has not been succesfully validated'
    CHANGED_MTIME: 'The meta information about this file has changed'
    UNKNOWN: 'This is an unknown file'
    UNKNOWN_MISSING: 'This was an unknown file, but has been removed manually'
    MISSING: 'This file is missing'
    MISSING_UNKNOWN: 'This file was missing, but has been added manually'
    MOVED_UNKNOWN: 'This file was moved, but has been added manually'
    MOVED_UNKNOWN_MISSING: 'This file was moved, then added manually, now removed manually'
    VALIDATING_HASH: 'This file is currently being validated, large files take a long time to process'
    REMOVED_UNKNOWN: 'This file, previously removed, has now been manually added'
    ZIPPED_UNKNOWN: 'This file was zipped and has been manually added'
    ZIPPED_UNKNOWN_MISSING: 'This file was zipped, was been manually added, now manually removed'
    INVLAID: 'Something went wrong with this file'

  errorType:
    CHANGED_HASH: 'hash'
    NO_HASH: 'hash'
    CHANGED_MTIME: 'file'
    UNKNOWN: 'file'
    UNKNOWN_MISSING: 'file'
    MISSING: 'file'
    MISSING_UNKNOWN: 'file'
    MOVED_UNKNOWN: 'file'
    MOVED_UNKNOWN_MISSING: 'file'
    REMOVED_UNKNOWN: 'file'
    ZIPPED_UNKNOWN: 'file'
    ZIPPED_UNKNOWN_MISSING: 'file'
    INVLAID: 'file'

  errorActions:
    CHANGED_HASH: 'accept'
    NO_HASH: 'validate'
    CHANGED_MTIME: 'validate'
    UNKNOWN: 'accept'
    UNKNOWN_MISSING: 'ignore'
    MISSING: 'ignore'
    MISSING_UNKNOWN: 'accept'
    MOVED_UNKNOWN: 'accept'
    MOVED_UNKNOWN_MISSING: 'ignore'
    VALIDATING_HASH: 'validate'
    REMOVED_UNKNOWN: 'accept'
    ZIPPED_UNKNOWN: 'accept'
    ZIPPED_UNKNOWN_MISSING: 'ignore'


  initialize: ->
    setInterval(
      () => do @model.fetch
      10000
    )

    @model.on 'sync', =>
      do @render
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

  initDropzone: ->
    model = @model.bind @
    render = @render.bind @

    @dropzone = new Dropzone '.dropzone',
      timeout: -1
      url: model.url() + '/add-upload-document'
      maxFilesize: 20 * 1000 * 1000
      autoQueue: yes
      previewTemplate: DropzoneFileTpl()
      previewsContainer: '.dropzone-files'
      clickable: '.fileinput-button'
      parallelUploads: 1
      init: ->
        @on 'addedfile', (file) ->
          last = $('.uploading').length - 1
          uploading = $($('.uploading')[last])
          id = file.name.replace(/[^\w?]/g, '-')
          uploading.addClass('uploading-' + id)
          uploading.find('.cancel').click => @removeFile file
        
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

  render: ->
    do @initDropzone if @dropzone == null && $('.dropzone-files').length

    uploadFiles = @model.get('uploadFiles')

    @globalAction('move-all', 'moveToDatastore')
    @globalAction('validate-all', 'validateFiles')
    @globalAction('zip')
    @globalAction('unzip')
    @globalAction('finish')

    do @renderChecksums
    do @renderZip

    for name of uploadFiles
        filesEl = $(".#{name}-files")
        filesEl.html('')
        if typeof uploadFiles[name].documents == 'undefined' && typeof uploadFiles[name].invalid == 'undefined'
            filesEl.append($("<h3 class='no-documents'>NO FILES IN #{@keyToName[name].toUpperCase()}</h3>"))
        for filename, data of uploadFiles[name].invalid
            data.message = @messages[data.type]
            data.errorType = @errorType[data.type] || ''
            data.hash = data.hash || 'NO HASH'
            data.action = @errorActions[data.type]
            data.el = "#{name}-#{data.id}"
            row = $(DocumentUploadFileRowTemplate data)
            filesEl.append(row)
        for filename, data of uploadFiles[name].documents
            data.errorType = 'valid'
            data.hash = data.hash || 'NO HASH'
            data.action = @keyToAction[name]
            data.el = "#{name}-#{data.id}"
            row = $(DocumentUploadFileRowTemplate data)
            filesEl.append(row)

        @fileAction('accept')
        @fileAction('delete')
        @fileAction('validate')
        @fileAction('ignore')
        @fileAction('move-metadata', 'move', 'supporting-documents')
        @fileAction('move-datastore', 'move', 'eidchub')
