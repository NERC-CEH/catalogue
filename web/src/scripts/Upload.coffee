define [
  'jquery'
  'backbone'
  'bootstrap'
  'cs!views/upload/hubbub/UploadView'
  'cs!models/upload/hubbub/Upload'
], (
    $, Backbone, Bootstrap, HubbubUploadView, HubbubUploadModel
) ->

  ###
  Initialize the Hubbub Uploader
  ###
  initialize: ->
    id = $('#document-upload').data('guid')
    model = new HubbubUploadModel()
    model.id = id
    model.set('id', id)
    new HubbubUploadView
      el: '#document-upload'
      model: model

    Backbone.history.start()