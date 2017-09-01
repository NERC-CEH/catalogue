MEGEBYTE = 1000000

define [
  'backbone'
  'tpl!templates/DropzoneRow.tpl',
  'tpl!templates/ChecksumRow.tpl'
], (Backbone, dropzoneRow, checksumRow) -> Backbone.View.extend
  initialize: (options) ->
    do @initDeleteButtons
    do @initFinish
    if $('.dz').length
      do @initDropzone
  
  updateChecksums: (response) ->
    data = []
    
    for name, value of response.data
      data.push checksumRow
        canDelete: $('#delete').length > 0
        canChangeType: $('#canChangeType').length > 0
        isData: 'checked'
        isMeta: ''
        filename: value.name
        md5Hash: value.hash
    
    meta = []
    
    for name, value of response.meta
      meta.push checksumRow
        canDelete: $('#delete').length > 0
        canChangeType: $('#canChangeType').length > 0
        isData: ''
        isMeta: 'checked'
        filename: value.name
        md5Hash: value.hash
    
    checksums = data.concat meta
    document.querySelector('.checksums-list').innerHTML = checksums.join('')
    do @initDeleteButtons
  
  initDeleteButtons: ->
    $('.delete').unbind 'click'
    $('.delete').click =>
      button = $(event.target)
      file = button.parent().parent().find('.checksum-file').text()
      $.ajax
        url: window.location.href + '/delete'
        type: 'POST'
        data:
          file: file
        headers:
          Accept: 'application/json'
        success: (res) =>
          @updateChecksums res
  
  disableFinish: (message) ->
    $('.finish-message').text(message)
    $('.finish').attr 'disabled', on
    icon = $('.finish .fa')
    icon.addClass('fa-ban')
    icon.removeClass('fa-check')
    icon.removeClass('fa-refresh')
    icon.removeClass('fa-spin')
  
  enableFinish: ->
    $('.finish-message').text('')
    $('.finish').attr 'disabled', off
    icon = $('.finish .fa')
    icon.addClass('fa-check')
    icon.removeClass('fa-ban')
    icon.removeClass('fa-refresh')
    icon.removeClass('fa-spin')
  
  submitFinish: ->
    $('.finish-message').text('')
    $('.finish').attr 'disabled', on
    icon = $('.finish .fa')
    icon.removeClass('fa-check')
    icon.removeClass('fa-ban-')
    icon.addClass('fa-refresh')
    icon.addClass('fa-spin')

  loadedDropzone: ->
    $('.dz .title').text 'Drag files here'
    $('.fileinput-button').attr 'disabled', off
    do @enableFinish
  
  toggleUploadCancelAll: (status) ->
    $('.upload-all').attr 'disabled', status
    $('.cancel-all').attr 'disabled', status

  addedFile: (file) ->
    @disableFinish 'Some files have not been uploaded yet'
    @toggleUploadCancelAll off
    $('.file-row').last().attr('id', file.id)
    $('#' + file.id + ' .upload').last().click => @dropzone.enqueueFile file
    $('#' + file.id + ' .cancel').last().click => @dropzone.removeFile file
    if file.size > 300 * MEGEBYTE
      $('#' + file.id + ' .max-size').last().removeClass 'is-inactive'
      $('#' + file.id + ' .max-size').last().addClass 'is-active'

  removedFile: ->
    if @dropzone.files.length == 0
      @toggleUploadCancelAll on
      do @enableFinish

  errorMessages:
    403: 'Unauthorized'
    409: 'Already exists'

  error: (file, message) ->
    progress = $('#' + file.id + ' .progress-bar')
    progress.width '100%'
    progress.removeClass 'progress-bar-success'
    progress.addClass 'progress-bar-danger'
    progress.text message

    upload = $('#' + file.id + ' .upload')
    upload.attr 'disabled', yes

    @disableFinish 'Resolve all issues below'
  
  success: (file, res) ->
    progress = $('#' + file.id + ' .progress-bar')
    progress.text 'Uploaded'
    setTimeout (=>
      @dropzone.removeFile file
      @updateChecksums res
      do @initDeleteButtons
    ), 500

  uploadAll: ->
    @dropzone.enqueueFiles @dropzone.getFilesWithStatus(Dropzone.ADDED)

  cancelAll: ->
    @toggleUploadCancelAll on
    @dropzone.removeAllFiles yes

  initDropzone: ->
    loadedDropzone = @loadedDropzone.bind this
    addedFile = @addedFile.bind this
    removedFile = @removedFile.bind this
    errorMessages = @errorMessages
    error = @error.bind this
    success = @success.bind this
    uploadAll = @uploadAll.bind this
    cancelAll = @cancelAll.bind this

    @dropzone = new Dropzone '.dz',
      url: window.location.href + '/add'
      maxFilesize: 1250
      autoQueue: no
      previewTemplate: dropzoneRow()
      previewsContainer: '#previews'
      clickable: '.fileinput-button'
      parallelUploads: 1
      init: ->
        do loadedDropzone
        fileCount = -1

        status = () =>
          @.files.map((f) -> f.status)

        @on 'addedfile', (file) ->
          fileCount += 1
          file.id = 'file-row-' + fileCount
          addedFile file

        @on 'removedfile', removedFile

        @on 'error', (file, errorMessage, xhr) ->
          message = errorMessage
          message = errorMessages[xhr.status] || errorMessage if xhr
          error file, message

        @on 'success', success

        $('.upload-all').click uploadAll

        $('.cancel-all').click cancelAll

  initFinish: ->
    $('.finish').click =>
      do @submitFinish
      $.ajax
        url: window.location.href + '/finish'
        type: 'POST'
        headers:
          Accept: 'application/json'
        success: (response) ->
          window.location.reload()
        fail: (error) =>
          do @enableFinish
          $('.finish-message').text('An error occured, if this persists then please contact an admin')