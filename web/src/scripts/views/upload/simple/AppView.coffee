define [
  'jquery'
  'backbone'
  'cs!collections/upload/simple/FileCollection'
  'cs!views/upload/simple/FileListView'
  'cs!views/upload/simple/MessageListView'
  'cs!views/upload/simple/UploadView'
], ($, Backbone, FileCollection, FileListView, MessageListView, UploadView) -> Backbone.View.extend

  initialize: (options) ->
    @files = new FileCollection
      url: options.url

    @messages = new Backbone.Collection()

    messageListView = new MessageListView
      el: '#messages'
      messages: @messages

    uploadView = new UploadView
      el: '#simple-upload-dropzone'
      files: @files
      messages: @messages

    fileListView = new FileListView
      el: '#files'
      files: @files
      messages: @messages

    $filesData = $('#files-data')
    @files.reset(JSON.parse($filesData.text())) if $filesData.length
    $messageData = $('#messages-data')
    @messages.reset(JSON.parse($messageData.text())) if $messageData.length
