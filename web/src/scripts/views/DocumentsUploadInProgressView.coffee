define [
  'backbone'
  'cs!views/DocumentUploadMessage'
  'tpl!templates/File.tpl'
  'tpl!templates/InvalidFile.tpl'
], (Backbone, message, fileTpl, invalidFileTpl) -> Backbone.View.extend
  el: '.folders'
  initialize: ->
    @model.on 'sync', =>
      do @initFolders
      do @initZip
      do @initMoveToDatastore
      do @render
      do $('.loading').remove
      $('.messages').hide 'fast'

    @model.on 'change', => do @render
    do @model.fetch

  initFolders: ->
    $('.documents .files, .plone .files, .datastore .files').sortable
      placeholder: 'ui-state-highlight'
      scroll: false
      connectWith: '.connectedSortable'
      cancel: '.empty-message, .file-invalid, .moving'
      stop: (evt, ui) =>
        item = $(ui.item)
        isDocuments = item.parent().parent().hasClass('documents')
        isPlone = item.parent().parent().hasClass('plone')
        isDatastore = item.parent().parent().hasClass('datastore')

        from = item.attr('id').split('-')[0]
        to = 'documents'
        to = 'datastore' if isDatastore
        to = 'plone' if isPlone

        if from != to and to != 'documents'
          file = item.data('filename')
          item.addClass('moving')
          item.attr('disabled', on)
          @model.move file, from, to
        else
          @model.set
            cancel: yes

  initZip: ->
    $('.zip, .unzip').unbind 'click'
    $('.zip, .unzip').click -> $('.zip, .unzip').attr('disabled', on)
    $('.zip').click => do @model.zip
    $('.unzip').click => do @model.unzip

  initMoveToDatastore: ->
    $('.move-to-datastore').unbind 'click'
    $('.move-to-datastore').attr 'disabled', off
    $('.move-to-datastore').click =>
      files = []
      $('.documents .file .filename-label').each (index, filename) ->
        files.push $(filename).text() if !$(filename).parent().parent().hasClass('file-invalid')

      if files.length > 0
        $('.move-to-datastore').attr 'disabled', on
        @model.moveToDatastore files

  render: ->
    do @renderZip
    do @renderAllFiles
    do @renderEmptyMessages
    $('.documents .files, .plone .files, .datastore .files').sortable 'cancel' if @model.get 'cancel'
    $('.move-to-datastore').attr 'disabled', off
    @renderModal @model.get 'modal' if @model.get 'modal'
    messageValue = @model.get 'message'
    message messageValue.message, messageValue.type, messageValue.timeout if messageValue
    if @model.get('message') != no or @model.get('cancel') != off or @model.get('modal') != off
      @model.set
        message: off
        cancel: no
        modal: off
    return this
    
  renderZip: ->
    $('.zip, .unzip').attr 'disabled', off
    if @model.get('uploadFiles').datastore && @model.get('uploadFiles').datastore.zipped
      $('.datastore-icon .fa').removeClass('fa-file-o')
      $('.datastore-icon .fa').addClass('fa-file-archive-o')
      do $('.zip').hide
      do $('.unzip').show
    else
      $('.datastore-icon .fa').removeClass('fa-file-archive-o')
      $('.datastore-icon .fa').addClass('fa-file-o')
      do $('.zip').show
      do $('.unzip').hide

  renderEmptyMessages: ->
    @renderEmptyMessage 'documents', '<span>Dropbox</span> is empty'
    @renderEmptyMessage 'plone', 'Move files here from <span>Dropbox</span> or <span>Datastore</span>'
    @renderEmptyMessage 'datastore', 'Move files here from <span>Dropbox</span> or <span>Metadata</span>'

  renderEmptyMessage: (folder, message) ->
    documens = @model.get('uploadFiles')[folder].documents || {}
    documens = (value for own prop, value of documens)
    invalid = @model.get('uploadFiles')[folder].invalid || {}
    invalid = (value for own prop, value of invalid)
    message = '' if documens.length > 0 || invalid.length > 0
    $('.' + folder + ' .empty-message').html message

  renderAllFiles: ->
    @renderFiles 'documents'
    @renderFiles 'plone'
    @renderFiles 'datastore'

  renderFiles: (folder) ->
    do $('.' + folder + ' .files .file').remove
    @renderValidFiles folder
    @renderInvalidFiles folder

  renderValidFiles: (folder) ->
    files = @model.get('uploadFiles')[folder].documents || {}
    files = (value for own prop, value of files)
    files.sort (left, right) ->
      return -1 if left.name < right.name
      return 1 if left.name > right.name
      return 0

    for path, file of files
      id = folder + '-' + file.id
      newFile = $(fileTpl
        path: file.path
        name: file.name,
        hash: file.hash,
        id: id)
      $('.' + folder + ' .files').append(newFile) if $('#' + id).length == 0
  
  renderInvalidFiles: (folder) ->
    files = @model.get('uploadFiles')[folder].invalid || {}
    files = (value for own prop, value of files)
    files.sort (left, right) ->
      return -1 if left.name < right.name
      return 1 if left.name > right.name
      return 0

    for path, file of files
      id = folder + '-' + file.id
      invalidFile = $(invalidFileTpl
        path: file.path
        comment: file.type
        type: file.type,
        name: file.name,
        hash: file.hash,
        id: id)
      $('.' + folder + ' .files').append(invalidFile) if $('#' + id).length == 0

    @invalidAction('accept')
    @invalidAction('ignore')
    @invalidAction('delete')
  
  invalidAction: (action) ->
    $('.' + action).unbind 'click'
    $('.' + action).click (evt) =>
      file = $(evt.target).parent().parent().parent()
      filename = file.data('filename')
      folder = file.attr('id').split('-')[0]
      @model.modal
      if action != 'delete'
        @model[action] folder, filename
      else
        @model.set 'modal',
          title: 'Delete <b>' + filename + '</b>'
          body: 'Are you sure you want to delete <b>' + filename + '</b>?'
          dismiss: 'No'
          accept: 'Yes'
          onAccept: =>
            @model[action] folder, filename
  
  renderModal: (modal) ->
    $('.modal-title').html modal.title
    $('.modal-body').html modal.body
    $('.modal-dismiss').html modal.dismiss
    $('.modal-accept').html modal.accept
    $('.modal-accept').unbind 'click'
    $('.modal-accept').click ->
      do modal.onAccept
      $('.modal-accept').unbind 'click'
