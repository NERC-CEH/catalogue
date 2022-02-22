define [
  'backbone'
  'dropzone'
  'filesize'
  'tpl!templates/upload/hubbub/DropzoneFileRow.tpl'
], (Backbone, Dropzone, filesize, template) -> Backbone.View.extend

  initialize: (options) ->
    el = options.el
    success = options.success
    url = options.url

    new Dropzone(el, @dropzoneOptions(url, success))

  dropzoneOptions: (url, success) ->
    timeout: -1
    url: url
    maxFilesize: 20 * 1000 * 1000
    autoQueue: yes
    previewTemplate: template()
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

      @on 'success', success

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