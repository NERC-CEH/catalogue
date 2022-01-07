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
#    TODO: load state of the app from html, state set from Jira state
    id = $('#document-upload').data('guid')
    model = new HubbubUploadModel()
    model.set('id', id)
    new HubbubUploadView
      el: '#document-upload'
      model: model