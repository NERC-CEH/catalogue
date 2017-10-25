MEGEBYTE = 1000000

define [
  'backbone'
  'tpl!templates/DocumentUploadMessage.tpl'
  'tpl!templates/DropzoneFile.tpl'
  'tpl!templates/File.tpl'
  'tpl!templates/InvalidFile.tpl'
], (Backbone, documentUploadMessageTpl, dropzoneFileTpl, fileTpl, invalidFileTpl) -> Backbone.View.extend
  initialize: (options) ->
    do @initFolders

    if $('.dropzone-files').length
      do @initDropzone
    else
      do $('.message.loading').remove
      $('.messages').hide 'fast' if $('.messages .message').length == 0
      do @updateInvalid

  updateInvalid: ->
    $('.file-invalid .delete, .file-invalid .ignore, .file-invalid .accept').attr('disabled', off)

    $('.file-invalid').each (index, file) =>
      filename = $(file).find('.filename-label').text()
      
      folder = $(file).parent().parent()
      name = 'documents'
      name = 'datastore' if folder.hasClass 'datastore'
      name = 'plone' if folder.hasClass 'plone'

      $(file).find('.delete').unbind 'click'
      $(file).find('.delete').click (evt) =>
        @modal 'Delete <b>' + filename + '</b>', 'Are you sure you want to delete <b>' + filename + '</b>', 'No', 'Yes', =>
          $.ajax
            url: window.location.href + '/delete/' + name
            type: 'POST'
            headers:
              Accept: 'application/json'
              Content: 'application/json'
            data:
              file: filename
            success: (res) =>
              @message 'Removed: ' + filename, 'ok', 3000
              @update res, filename, 'documents'
              @update res, filename, 'datastore'
              @update res, filename, 'plone'
            error: (error) =>
              if error.responseText
                @message 'Failed to remove: ' + filename + ', ' + error.responseText, 'warning', 3000
              else
                @message 'Failed to remove: ' + filename, 'warning', 3000

      $(file).find('.ignore').unbind 'click'
      $(file).find('.ignore').click (evt) =>
        $.ajax
          url: window.location.href + '/delete/' + name
          type: 'POST'
          headers:
            Accept: 'application/json'
            Content: 'application/json'
          data:
            file: filename
          success: (res) =>
            @message 'Ignored: ' + filename, 'ok', 3000
            @update res, filename, 'documents'
            @update res, filename, 'datastore'
            @update res, filename, 'plone'
          error: (error) =>
            if error.responseText
              @message 'Failed to ignore: ' + filename + ', ' + error.responseText, 'warning', 3000
            else
              @message 'Failed to ignore: ' + filename, 'warning', 3000

      $(file).find('.accept').unbind 'click'
      $(file).find('.accept').click (evt) =>
        $.ajax
          url: window.location.href + '/accept-invalid/' + name
          type: 'POST'
          headers:
            Accept: 'application/json'
            Content: 'application/json'
          data:
            file: filename
          success: (res) =>
            @message 'Accepted: ' + filename, 'ok', 3000
            @update res, filename, 'documents'
            @update res, filename, 'datastore'
            @update res, filename, 'plone'
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
            url: window.location.href + '/delete/documents'
            type: 'POST'
            headers:
              Accept: 'application/json'
              Content: 'application/json'
            data:
              file: filename
            success: (res) =>
              @message 'Removed: ' + filename, 'ok', 3000
              @update res, filename, 'documents'
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

    $('.finish').attr('disabled', $('.documents .file').length == 0)

    $('.file-invalid .delete').attr('disabled', off)
    $('.file-invalid .ignore').attr('disabled', off)
    $('.file-invalid .accept').attr('disabled', off)

    if $('.dropzone-files').length == 0
      $('.documents .files, .plone .files, .datastore .files').sortable
        placeholder: 'ui-state-highlight'
        scroll: false
        connectWith: '.connectedSortable'
        cancel: '.empty-message, .file-invalid'
        stop: (evt, ui) =>
          item = $(ui.item)
          isDocuments = item.parent().parent().hasClass('documents')
          isPlone = item.parent().parent().hasClass('plone')
          isDatastore = item.parent().parent().hasClass('datastore')

          from = item.attr('id').split('-')[0]
          to = 'plone'
          to = 'datastore' if isDatastore

          file = item.find('.filename-label').text()

          if isDocuments
            $('.documents .files, .plone .files, .datastore .files').sortable 'cancel'
          else if from != to
            $.ajax
              url: window.location.href + '/move'
              type: 'POST'
              headers:
                Accept: 'application/json'
              data:
                file: file
                from: from
                to: to
              success: (res) =>
                  currentId = item.attr('id')
                  item.attr('id', currentId.replace(from, to))
                  @message 'Moved: from <b>' + from + '/' + file + '</b> to <b>' + to + '/' + file + '</b>', 'success', 3000
              error: (error) =>
                do @dropzone.enable
                if error.responseText
                  @message 'Could not move: ' + error.responseText, 'warning'
                else
                  @message 'Could not move' + error.responseText, 'warning'

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
    do @updateInvalid
   
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

  update: (res, file, name) ->
    if file and file.name
      @message 'Uploaded: ' + file.name, 'ok', 3000
    files = res[name].files || []
    invalid = res[name].invalid || {}
    invalid = (value for own prop, value of invalid)
    all = files.concat invalid
    console.log(name, all)

    for index, file of files
      fileElement = $('#' + name + '-' + file.id)
      if fileElement.length == 0
        newFile = $(fileTpl
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
    do @updateInvalid

    if $('.' + name + ' .file').length > 0
      $('.' + name + ' .empty-message').text('')
    else
      $('.' + name + ' .empty-message').html('Drag files into <u>here</u> to upload')
  
    $('.finish').attr('disabled', $('.' + name + ' .file').length == 0)

  initDropzone: ->
    update = @update.bind this
    message = @message.bind this

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