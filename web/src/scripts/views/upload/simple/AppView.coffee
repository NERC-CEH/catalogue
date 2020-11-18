define [
  'jquery'
  'backbone'
  'cs!collections/upload/simple/FileCollection'
  'cs!views/upload/simple/FilesView'
  'cs!views/upload/simple/MessagesView'
  'cs!views/upload/simple/UploadView'
], ($, Backbone, FileCollection, FilesView, MessagesView, UploadView) -> Backbone.View.extend

  initialize: (options) ->
    @files = new FileCollection
      url: options.url

    @messages = new Backbone.Collection()

    messagesView = new MessagesView
      el: '#messages'
      messages: @messages

    uploadView = new UploadView
      el: '#simple-upload-dropzone'
      files: @files
      messages: @messages

    filesView = new FilesView
      el: '#files'
      files: @files
      messages: @messages

    @files.reset(JSON.parse($('#files-data').text()))
    $message = $('#messages-data')
    @messages.reset(JSON.parse($message.text())) if $message.length
