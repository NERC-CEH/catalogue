define [
  'backbone'
  'tpl!templates/Dropzone.tpl'
  'tpl!templates/DropzoneRow.tpl'
], (Backbone, template, row) -> Backbone.View.extend
  initialize: (options) ->
    do @render

  render: ->
    @$el.html template(@model.attributes)
    do @initDropzone
    @
  
  initDropzone: ->
    dropzone = new Dropzone '.dz',
      url: '/target-url'
      parallelUploads: 20
      autoQueue: no
      previewTemplate: row()
      previewsContainer: '#previews'
      clickable: '.fileinput-button'
      init: ->
        fileCount = -1
        @on 'addedfile', (file) ->
          fileCount += 1
          file.id = 'file-row-' + fileCount
          $('.file-row').last().attr('id', file.id)
          $('#' + file.id + ' .upload').last().click ->
            dropzone.enqueueFile file
          $('#' + file.id + ' .cancel').last().click ->
            dropzone.removeFile file
          
          if file.size > 100
            $('#' + file.id + ' .max-size').last().removeClass('is-inactive')
            $('#' + file.id + ' .max-size').last().addClass('is-active')

        @on 'error', (file, errorMessage, xhr) ->
          progress = $('#' + file.id + ' .progress-bar')
          progress.width('100%')
          progress.removeClass('progress-bar-success')
          progress.addClass('progress-bar-danger')
          message = 'Failed'
          progress.text(message)

        $('.upload-all').click ->
          dropzone.enqueueFiles dropzone.getFilesWithStatus(Dropzone.ADDED)
        $('.cancel-all').click ->
          dropzone.removeAllFiles yes

    window.dropzone = dropzone