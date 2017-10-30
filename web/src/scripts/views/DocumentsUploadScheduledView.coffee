define [
  'backbone'
  'cs!views/DocumentUploadMessage'
  'tpl!templates/DropzoneFile.tpl'
  'tpl!templates/DeleteableFile.tpl'
  'tpl!templates/InvalidFile.tpl'
], (Backbone, message, dropzoneFileTpl, deleteableFileTpl, invalidFileTpl) -> Backbone.View.extend
  initialize: ->
    @model.on 'sync', => do @render
    @model.on 'change', => do @render
    @model.save null,
      url: window.location.href + '/get'

    do $('.loading').remove
    $('.messages').hide 'fast'

    do @initFinish
    do @initDropzone

  initFinish: ->
    $('.finish').attr 'disabled', off
    $('.finish').click =>
      @model.set 'modal',
        title: 'Finish'
        body: 'Once you have finished you will not be able to add, remove or update files. Are you sure you want to finish?'
        dismiss: 'No'
        accept: 'Yes'
        onAccept: =>
          do @model.finish
          do @dropzone.disable
          $('.file, .finish').attr 'disabled', on
          message 'Finish in progress, please wait', 'loading'
          @finishFirstTimeout = setTimeout ->
            message 'This is taking a while, please wait', 'warning', 3000
          , 3000
          @finishSecondTimeout = setTimeout ->
            message 'Taking too long, please refresh and try again. If problem persists please contact an admin', 'warning'
          , 6000

  initDropzone: ->
    model = @model.bind @
    render = @render.bind @

    @dropzone = new Dropzone '.dropzone-files',
      url: window.location.href + '/add/documents'
      maxFilesize: 1250
      autoQueue: yes
      previewTemplate: dropzoneFileTpl()
      previewsContainer: '.dropzone-files'
      clickable: '.fileinput-button'
      parallelUploads: 1
      init: ->
        $('.finish').attr 'disabled', on
        $('.file .delete, .fileinput-button').attr 'disabled', off

        @on 'addedfile', (file) ->
          $('.finish').attr('disabled', on)
          $('.documents .empty-message').text('')
          last = $('.uploading').length - 1
          uploading = $($('.uploading')[last])
          id = file.name.replace(/[^\w?]/g, '-')
          uploading.addClass('uploading-' + id)
          uploading.find('.cancel').click => @removeFile file
        
        @on 'success', (file, res) ->
          model.uploaded file, res
          do render
        
        @on 'error', (file, errorMessage, xhr) ->
          id = file.name.replace(/[^\w?]/g, '-')
          $('.uploading-' + id).remove()
          errorMessages =
            0: 'No connection'
            403: 'Unauthorized'
          errorMessage = errorMessage
          errorMessage = errorMessages[xhr.status] || errorMessage if xhr
          model.set 'message',
            message: 'Could not upload <b>' + file.name + '</b> because <b>' + errorMessage + '</b>'
            type: 'warning'
            timeout: 3000
  
  render: ->
    do @renderFinish

    do @renderEmptyMessage

    messageValue = @model.get 'message'
    message messageValue.message, messageValue.type, messageValue.timeout if messageValue

    @renderModal @model.get 'modal' if @model.get 'modal'

    if @model.get('message') != no or @model.get('modal') != off
      @model.set
        message: off
        modal: off

    do @renderFiles

  renderFinish: ->
    $('.finish').attr 'disabled', off if $('.uploading').length == 0

  renderEmptyMessage: ->
    emptyMessage = 'Drag files into <u>here</u> to upload'
    emptyMessage = '' if @model.get('documents').files
    $('.documents .empty-message').html emptyMessage

  renderModal: (modal) ->
    $('.modal-title').html modal.title
    $('.modal-body').html modal.body
    $('.modal-dismiss').html modal.dismiss
    $('.modal-accept').html modal.accept
    $('.modal-accept').unbind 'click'
    $('.modal-accept').click ->
      do modal.onAccept
      $('.modal-accept').unbind 'click'
  
  renderFiles: ->
    do $('.file:not(".uploading")').remove

    for index, file of @model.get('documents').files
      do $('.uploading-' + file.id).remove
      newFile = $(deleteableFileTpl
        name: file.name,
        id: 'documents-' + file.id)
      $('.files').append(newFile)
    
    $('.delete').unbind 'click'
    $('.delete').click (evt) =>
      target = $(evt.target).parent().parent()
      filename = target.find('.filename-label').text()
      @model.set 'modal',
        title: 'Delete <b>' + filename + '</b>'
        body: 'Are you sure you want to perminatly delete <b>' + filename + '</b>'
        dismiss: 'No'
        accept: 'Yes'
        onAccept: =>
          @model.delete filename
