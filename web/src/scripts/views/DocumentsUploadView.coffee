MEGEBYTE = 1000000

define [
  'backbone'
  'tpl!templates/DocumentUploadMessage.tpl'
  'tpl!templates/DropzoneFile.tpl'
  'tpl!templates/File.tpl'
], (Backbone, documentUploadMessageTpl, dropzoneFileTpl, fileTpl) -> Backbone.View.extend
  initialize: (options) ->
    do @initFolders

    if $('.dropzone-files').length
      do @initDropzone
    else
      do $('.message.loading').remove
      $('.messages').hide 'fast' if $('.messages .message').length == 0
      do @initInvalid

  initInvalid: ->
    $('.invalid .delete, .invalid .ignore, .invalid .accept').attr('disabled', off)

    $('.invalid .file').each (index, file) =>
      filename = $(file).find('.filename-label').text()
      $(file).find('.delete').click (evt) =>
        @modal 'Delete <b>' + filename + '</b>', 'Are you sure you want to delete <b>' + filename + '</b>', 'No', 'Yes', =>
          $.ajax
            url: window.location.href + '/delete'
            type: 'POST'
            headers:
              Accept: 'application/json'
              Content: 'application/json'
            data:
              file: filename
            success: (res) =>
              @message 'Removed: ' + filename, 'ok', 3000
              @update res, filename
            error: (error) =>
              if error.responseText
                @message 'Failed to remove: ' + filename + ', ' + error.responseText, 'warning', 3000
              else
                @message 'Failed to remove: ' + filename, 'warning', 3000

      $(file).find('.ignore').click (evt) =>
        $.ajax
          url: window.location.href + '/delete'
          type: 'POST'
          headers:
            Accept: 'application/json'
            Content: 'application/json'
          data:
            file: filename
          success: (res) =>
            @message 'Ignored: ' + filename, 'ok', 3000
            @update res, filename
          error: (error) =>
            if error.responseText
              @message 'Failed to ignore: ' + filename + ', ' + error.responseText, 'warning', 3000
            else
              @message 'Failed to ignore: ' + filename, 'warning', 3000

      $(file).find('.accept').click (evt) =>
        $.ajax
          url: window.location.href + '/accept-invalid'
          type: 'POST'
          headers:
            Accept: 'application/json'
            Content: 'application/json'
          data:
            file: filename
          success: (res) =>
            @message 'Accepted: ' + filename, 'ok', 3000
            @update res, filename
          error: (error) =>
            if error.responseText
              @message 'Failed to accept: ' + filename + ', ' + error.responseText, 'warning', 3000
            else
              @message 'Failed to accept: ' + filename, 'warning', 3000

  updateDeleteDocuments: ->
    $('.documents .file').each (index, file) =>
      filename = $(file).find('.filename-label').text()
      $(file).unbind('click')
      $(file).find('.delete').click (evt) =>
        @modal 'Delete <b>' + filename + '</b>', 'Are you sure you want to delete <b>' + filename + '</b>', 'No', 'Yes', =>
          $.ajax
            url: window.location.href + '/delete'
            type: 'POST'
            headers:
              Accept: 'application/json'
              Content: 'application/json'
            data:
              file: filename
            success: (res) =>
              @message 'Removed: ' + filename, 'ok', 3000
              @update res, filename
            error: (error) =>
              if error.responseText
                @message 'Failed to remove: ' + filename + ', ' + error.responseText, 'warning', 3000
              else
                @message 'Failed to remove: ' + filename, 'warning', 3000

  initFolders: ->
    if $('.documents .file').length > 0
      $('.documents .empty-message').text('')
    else if $('.dropzone-files').length > 0
      $('.documents .empty-message').html('Drag files into <u>here</u> to upload')
    else
      $('.documents .empty-message').html('No files in <u>Documents</u>')

    if $('.invalid .file').length > 0
      $('.invalid .empty-message').text('')
    else
      $('.invalid .empty-message').html('No invalid files')

    $('.finish').attr('disabled', $('.documents .file').length == 0)

    $('.invalid .delete').attr('disabled', off)
    $('.invalid .ignore').attr('disabled', off)
    $('.invalid .accept').attr('disabled', off)

    if $('.dropzone-files').length == 0
      $('.documents .files, .plone .files, .datastore .files').sortable
        placeholder: 'ui-state-highlight'
        scroll: false
        connectWith: '.connectedSortable'
        cancel: '.empty-message'
        stop: (evt, ui) ->
          item = $(ui.item)
          isDocuments = item.parent().parent().hasClass('documents')
          isPlone = item.parent().parent().hasClass('plone')
          isDatastore = item.parent().parent().hasClass('datastore')

          $(this).sortable 'cancel' if isDocuments
          console.log('move to plone') if isPlone
          console.log('move to datastore') if isDatastore

          if $('.documents .file').length > 0
            $('.documents .empty-message').text('')
          else if $('.dropzone-files').length > 0
            $('.documents .empty-message').html('Drag files into <u>here</u> to upload')
          else
            $('.documents .empty-message').html('No files in <u>Documents</u>')

          if $('.plone .file').length > 0
            $('.plone .empty-message').text('')
          else
            $('.plone .empty-message').html('Drag files from <u>Documents</u> or <u>Datastore</u>')

          if $('.datastore .file').length > 0
            $('.datastore .empty-message').text('')
          else
            $('.datastore .empty-message').html('Drag files from <u>Documents</u> or <u>Plone</u>')
      
      do $('.documents .files, .plone .files, .datastore .files').disableSelection

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
              @message 'Could not finish: ' + error.responseText, 'warning'
            else
              @message 'Could not finish' + error.responseText, 'warning'
          always: =>
            @message 'Finishing please wait ...', 'loading', 5000
            setTimeout =>
              @message 'It seems to be taking a while to Finish. Please <b>refresh the page and try again</b>. If there is no <b>Finish</b> button then the issue has moved on.', 'warning'
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
  
  message: (message, type, timeout) ->
    message = documentUploadMessageTpl
      type: type
      message: message
    message = $(message)
    message.find('.close').click ->
      do message.remove
    messages = $('.messages')
    messages.show 'fast' if !messages.is ':visible'
    messages.append message
    messages.scrollTop 30 * messages.find('.message').length
    if timeout
      setTimeout ->
        message.hide 'fast', ->
          do message.remove
          messages.hide 'fast' if messages.find('.message').length == 0
      , timeout

  update: (res, file) ->
    if file and file.name
      @message 'Uploaded: ' + file.name, 'ok', 3000
    files = res.files || []

    for index, file of files
      fileElement = $('#' + file.id)
      if fileElement.length == 0
        newFile = $(fileTpl
          name: file.name,
          hash: file.hash,
          id: file.id)
        $('.documents .files').append(newFile)
    $('.documents .file, .invalid .file').each (index, file) ->
      filename = $(file).find('.filename-label').text()
      matching = files.filter (rfile) ->
        return rfile.name == filename
      if matching.length == 0
        $(file).remove()

    do @updateDeleteDocuments

    if $('.documents .file').length > 0
      $('.documents .empty-message').text('')
    else
      $('.documents .empty-message').html('Drag files into <u>here</u> to upload')
    
    if $('.invalid .file').length > 0
      $('.invalid .empty-message').text('')
    else
      $('.invalid .empty-message').html('No invalid files')
  
    $('.finish').attr('disabled', $('.documents .file').length == 0)

  initDropzone: ->
    update = @update.bind this
    message = @message.bind this

    dropzone = new Dropzone '.dropzone-files',
      url: window.location.href + '/add'
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
          update res, dropzone, file
        
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