define [
  'backbone'
  'cs!views/DocumentUploadMessage'
  'tpl!templates/File.tpl'
  'tpl!templates/InvalidFile.tpl'
], (Backbone, message, fileTpl, invalidFileTpl) -> Backbone.View.extend
  initialize: (options) ->
    do @initFolders
    do $('.message.loading').remove
    $('.messages').hide 'fast' if $('.messages .message').length == 0
    do @updateInvalid
    do @initMoveToDatastore
    do @initZip

  initZip: ->
    $('.zip, .unzip').attr('disabled', off)
    $('.zip').click ->
      $('.zip').attr('disabled', on)
      $.ajax
        url: window.location.href + '/zip/datastore'
        type: 'POST'
        headers:
          Accept: 'application/json'
        success: (res) ->
            $('.zip, .unzip').attr('disabled', off)
            $('.zip').hide()
            $('.unzip').show()
            message 'Zipped <u>Datastore</u>', 'success', 3000
        error: (error) ->
          $('.zip, .unzip').attr('disabled', off)
          if error.responseText
            message 'Could not zip <u>Datastore</u> because ' + error.responseText, 'warning', 3000
          else
            message 'Could not zip <u>Datastore</u>', 'warning', 3000

    $('.unzip').click ->
      $('.unzip').attr('disabled', on)
      $.ajax
        url: window.location.href + '/unzip/datastore'
        type: 'POST'
        headers:
          Accept: 'application/json'
        success: (res) ->
            $('.zip, .unzip').attr('disabled', off)
            $('.unzip').hide()
            $('.zip').show()
            message 'Unzipped <u>Datastore</u>', 'success', 3000
        error: (error) ->
          $('.zip, .unzip').attr('disabled', off)
          if error.responseText
            message 'Could not unzip <u>Datastore</u> because ' + error.responseText, 'warning', 3000
          else
            message 'Could not unzip <u>Datastore</u>', 'warning', 3000

  initMoveToDatastore: ->
    $('.move-to-datastore').attr('disabled', off)
    $('.move-to-datastore').click =>
      $('.move-to-datastore').attr('disabled', on)
      filenameLabels = $('.documents .file .filename-label')
      files = []
      filenameLabels.each (index, filenameLabel) ->
        files.push $(filenameLabel).text()
      $.ajax
        url: window.location.href + '/move-all'
        type: 'POST'
        headers:
          Accept: 'application/json'
        data:
          files: files
          from: 'documents'
          to: 'datastore'
        success: (res) =>
            @update res, undefined, 'documents'
            @update res, undefined, 'datastore'
            @update res, undefined, 'plone'
            message 'Moved: from <b>' + files.join(', ') + '</b> to <u>Datastore</u>', 'success', 3000
        error: (error) ->
          $('.move-to-datastore').attr('disabled', off)
          $('.documents .files, .plone .files, .datastore .files').sortable 'cancel'
          if error.responseText
            message 'Could not move: <b>' + files.join(', ') + '</b> because ' + error.responseText, 'warning', 3000
          else
            message 'Could not move: <b>' + files.join(', ') + '</b>', 'warning', 3000

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
              message 'Removed: ' + filename, 'success', 3000
              @update res, filename, 'documents'
              @update res, filename, 'datastore'
              @update res, filename, 'plone'
            error: (error) ->
              if error.responseText
                message 'Failed to remove: ' + filename + ', ' + error.responseText, 'warning', 3000
              else
                message 'Failed to remove: ' + filename, 'warning', 3000

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
            message 'Ignored: ' + filename, 'success', 3000
            @update res, filename, 'documents'
            @update res, filename, 'datastore'
            @update res, filename, 'plone'
          error: (error) ->
            if error.responseText
              message 'Failed to ignore: ' + filename + ', ' + error.responseText, 'warning', 3000
            else
              message 'Failed to ignore: ' + filename, 'warning', 3000

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
            message 'Accepted: ' + filename, 'success', 3000
            @update res, filename, 'documents'
            @update res, filename, 'datastore'
            @update res, filename, 'plone'
          error: (error) ->
            if error.responseText
              message 'Failed to accept: ' + filename + ', ' + error.responseText, 'warning', 3000
            else
              message 'Failed to accept: ' + filename, 'warning', 3000

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

  initFolders: ->
    if $('.documents .file').length > 0
      $('.documents .empty-message').text('')
    else
      $('.documents .empty-message').html('No files in <u>Documents</u>')

    $('.file-invalid .delete').attr('disabled', off)
    $('.file-invalid .ignore').attr('disabled', off)
    $('.file-invalid .accept').attr('disabled', off)

    $('.documents .files, .plone .files, .datastore .files').sortable
      placeholder: 'ui-state-highlight'
      scroll: false
      connectWith: '.connectedSortable'
      cancel: '.empty-message, .file-invalid, .moving'
      stop: (evt, ui) ->
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
          item.addClass('moving')
          item.attr('disabled', on)
          $.ajax
            url: window.location.href + '/move'
            type: 'POST'
            headers:
              Accept: 'application/json'
            data:
              file: file
              from: from
              to: to
            success: (res) ->
                currentId = item.attr('id')
                item.attr('id', currentId.replace(from, to))
                item.removeClass('moving')
                item.attr('disabled', off)
                message 'Moved: from <b>' + from + '/' + file + '</b> to <b>' + to + '/' + file + '</b>', 'success', 3000
            error: (error) ->
              item.removeClass('moving')
              item.attr('disabled', off)
              $('.documents .files, .plone .files, .datastore .files').sortable 'cancel'
              if error.responseText
                message 'Could not move: ' + file + ' because ' + error.responseText, 'warning'
              else
                message 'Could not move: ' + file, 'warning'

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