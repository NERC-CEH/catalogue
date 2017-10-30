define [
  'backbone'
  'cs!views/DocumentUploadMessage'
  'tpl!templates/DropzoneFile.tpl'
  'tpl!templates/DeleteableFile.tpl'
  'tpl!templates/InvalidFile.tpl'
], (Backbone, message, dropzoneFileTpl, deleteableFileTpl, invalidFileTpl) -> Backbone.View.extend
  initialize: (options) ->
    do @initDcouments
    do @initDropzone

  updateDeleteDocuments: ->
    $('.documents .file').each (index, file) =>
      filename = $(file).find('.filename-label').text()
      $(file).unbind('click')
      $(file).find('.delete').click (evt) =>
        @modal 'Delete <b>' + filename + '</b>', 'Are you sure you want to delete <b>' + filename + '</b>', 'No', 'Yes', =>
          $.ajax
            url: window.location.href + '/delete/documents'
            type: 'POST'
            headers:
              Accept: 'application/json'
              Content: 'application/json'
            data:
              file: filename
            success: (res) =>
              message 'Removed: ' + filename, 'success', 3000
              @update res, filename, 'documents'
            error: (error) ->
              if error.responseText
                message 'Failed to remove: ' + filename + ', ' + error.responseText, 'warning', 3000
              else
                message 'Failed to remove: ' + filename, 'warning', 3000

  initDcouments: ->
    if $('.documents .file').length > 0
      $('.documents .empty-message').text('')
    else
      $('.documents .empty-message').html('Drag files into <u>here</u> to upload')

    $('.finish').attr('disabled', $('.documents .file').length == 0)

    do @updateDeleteDocuments
   
    $('.documents .finish').unbind('click')
    $('.documents .finish').click =>
      @modal 'Finish Uploading', 'Have you finished upload all your documents? <br /> This will stop you from uploading anymore and progress this issue futher.', 'No', 'Yes', =>
        $('.delete').attr('disabled', on)
        $('.fileinput-button').attr('disabled', on)
        $('.finish').attr('disabled', on)
        do @dropzone.disable
        $.ajax
          url: window.location.href + '/finish'
          type: 'POST'
          headers:
            Accept: 'application/json'
          success: (res) ->
            do window.location.reload
          error: (error) =>
            $('.delete').attr('disabled', off)
            $('.fileinput-button').attr('disabled', off)
            $('.finish').attr('disabled', off)
            do @dropzone.enable
            if error.responseText
              message 'Could not finish: ' + error.responseText, 'warning'
            else
              message 'Could not finish' + error.responseText, 'warning'
          always: ->
            message 'Finishing please wait ...', 'loading', 5000
            setTimeout ->
              message 'It seems to be taking a while to Finish. Please <b>refresh the page and try again</b>. If there is no <b>Finish</b> button then the issue has moved on.', 'warning'
            , 5000

  modal: (title, body, dismissText, acceptText, onAccept) ->
    $('.modal-title').html(title)
    $('.modal-body').html(body)
    $('.modal-dismiss').html(dismissText)
    $('.modal-accept').html(acceptText)

    $('.modal-accept').unbind('click')
    $('.modal-accept').click ->
      do onAccept
      $('.modal-accept').unbind('click')

  update: (res, file, name) ->
    if file and file.name
      message 'Uploaded: ' + file.name, 'success', 3000
    files = res[name].files || []
    invalid = res[name].invalid || {}
    invalid = (value for own prop, value of invalid)
    all = files.concat invalid

    for index, file of files
      fileElement = $('#' + name + '-' + file.id)
      if fileElement.length == 0
        newFile = $(deleteableFileTpl
          name: file.name,
          hash: file.hash,
          id: name + '-' + file.id)
        $('.' + name + ' .files').append(newFile)
      else if fileElement.hasClass('file-invalid')
        fileElement.find('.invalid-container').remove()
        fileElement.removeClass('file-invalid')
    
    for index, file of invalid
      fileElement = $('#' + name + '-' + file.id)
      if fileElement.length == 0
        newFile = $(invalidFileTpl
          comment: file.comments[file.comments.length - 1]
          type: file.type,
          name: file.name,
          hash: file.hash,
          id: name + '-' + file.id)
        newFile.addClass 'file-invalid'
        $('.' + name + ' .files').append(newFile)
    
    $('.' + name + ' .file, .invalid .file').each (index, file) ->
      filename = $(file).find('.filename-label').text()
      matching = all.filter (rfile) ->
        return rfile.name == filename
      if matching.length == 0
        $(file).remove()

    do @updateDeleteDocuments

    if $('.' + name + ' .file').length > 0
      $('.' + name + ' .empty-message').text('')
    else
      $('.' + name + ' .empty-message').html('Drag files into <u>here</u> to upload')
  
    $('.finish').attr('disabled', $('.' + name + ' .file').length == 0)

  initDropzone: ->
    update = @update.bind this
    message = message.bind this

    dropzone = new Dropzone '.dropzone-files',
      url: window.location.href + '/add/documents'
      maxFilesize: 1250
      autoQueue: yes
      previewTemplate: dropzoneFileTpl()
      previewsContainer: '.dropzone-files'
      clickable: '.fileinput-button'
      parallelUploads: 1
      init: ->
        $('.file .delete, .fileinput-button').attr('disabled', off)
        do $('.message.loading').remove
        $('.messages').hide 'fast' if $('.messages .message').length == 0

        @on 'addedfile', (file) ->
          $('.documents .empty-message').text('')

          $('.finish').attr('disabled', on)

          last = $('.uploading').length - 1
          uploading = $($('.uploading')[last])
          id = file.name.replace(/[^\w?]/g, '_')
          uploading.addClass('uploading-' + id)
          uploading.find('.cancel').click ->
            dropzone.removeFile file

        @on 'success', (file, res) ->
          update res, file, 'documents'
        
        @on 'error', (file, errorMessage, xhr) ->
          errorMessages =
            0: 'No connection'
            403: 'Unauthorized'

          progressMessage = errorMessage
          progressMessage = errorMessages[xhr.status] || errorMessage if xhr
          
          message 'Could not upload: <b>' + file.name + '</b>, ' + errorMessage, 'warning', 3000

          progressMessage = 'Too big' if progressMessage.indexOf 'too big' != -1

          id = file.name.replace(/[^\w?]/g, '_')
          progress = $('.uploading-' + id + ' .progress-bar')
          progress.width '100%'
          progress.removeClass 'progress-bar-success'
          progress.addClass 'progress-bar-danger'
          progress.text progressMessage

    @dropzone = dropzone