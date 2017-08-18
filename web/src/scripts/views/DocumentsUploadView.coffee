define [
  'backbone'
  'tpl!templates/DropzoneRow.tpl',
  'tpl!templates/ChecksumRow.tpl'
], (Backbone, dropzoneRow, checksumRow) -> Backbone.View.extend
  initialize: (options) ->
    if $('.dz').length
      do @initDeleteButtons
      do @initDropzone
      do @initFinish
  
  updateChecksums: (files) ->
    checksums = files
      .map (d) ->
        checksumRow
          canDelete: $('#delete').length > 0
          filename: d.filename
          md5Hash: d.md5Hash
      .join ''

    document.querySelector('.checksums-list').innerHTML = checksums
  
  initDeleteButtons: ->
    $('.delete').click (event) ->
      button = $(event.target)
      file = button.attr 'data-file'
      $.ajax
        url: window.location.href + '/' + file
        type: 'DELETE'
        success: ->
          do button.parent().parent().remove

  loadedDropzone: ->
    $('.dz .title').text 'Drag files here'
    $('.fileinput-button').attr 'disabled', off
  
  toggleUploadCancelAll: (status) ->
    $('.upload-all').attr 'disabled', status
    $('.cancel-all').attr 'disabled', status

  addedFile: (file) ->
    @toggleUploadCancelAll off
    $('.file-row').last().attr('id', file.id)
    $('#' + file.id + ' .upload').last().click => @dropzone.enqueueFile file
    $('#' + file.id + ' .cancel').last().click => @dropzone.removeFile file
    if file.size > 1000
      $('#' + file.id + ' .max-size').last().removeClass 'is-inactive'
      $('#' + file.id + ' .max-size').last().addClass 'is-active'

  removedFile: ->
    if @dropzone.files.length == 0
      @toggleUploadCancelAll on

  errorMessages:
    default: 'Failed'
    403: 'Unauthorized'
    409: 'Already exists'

  error: (file, message) ->
    progress = $('#' + file.id + ' .progress-bar')
    progress.width '100%'
    progress.removeClass 'progress-bar-success'
    progress.addClass 'progress-bar-danger'
    progress.text message
  
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
      parallelUploads: 20
      autoQueue: no
      previewTemplate: dropzoneRow()
      previewsContainer: '#previews'
      clickable: '.fileinput-button'
      init: ->
        do loadedDropzone
        fileCount = -1

        @on 'addedfile', (file) ->
          fileCount += 1
          file.id = 'file-row-' + fileCount
          addedFile file

        @on 'removedfile', removedFile

        @on 'error', (file, errorMessage, xhr) ->
          message = errorMessages[xhr.status] || errorMessages.default
          error file, message

        @on 'success', success

        $('.upload-all').click uploadAll

        $('.cancel-all').click cancelAll

  initFinish: ->
    $('#finish').click ->
      $.ajax
        url: window.location.href + '/finish'
        type: 'POST'
        headers:
          Accept: 'application/json'
        success: (response) ->
          console.log(response)