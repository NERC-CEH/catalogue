define [
  'jquery'
  'underscore'
  'backbone'
  'cs!collections/upload/simple/Files'
  'cs!views/upload/simple/FileView'
  'cs!views/upload/simple/ToolsView'
  'cs!views/upload/simple/MessageView'
  'cs!views/upload/simple/UploadView'
], ($, _, Backbone, Files, FileView, ToolsView, MessageView, UploadView) -> Backbone.View.extend

  initialize: (options) ->
    @files = new Files
      url: options.url

    @messages = new Backbone.Collection()

    @$fileList = @$('#files')
    @$messageList = @$('#messages')

    @listenTo(@files, 'add', @addOne)
    @listenTo(@files, 'reset', @addAll)
    @listenTo(@messages, 'add', @addMessage)

    @files.reset(JSON.parse($('#data').text()))

    uploadView = new UploadView
      el: '#simple-upload-dropzone'
      files: @files
      messages: @messages

    filesTools = new ToolsView
      el: '#filesTools'
      files: @files
      messages: @messages

  addOne: (file) ->
    view = new FileView({model: file})
    @$fileList.append(view.render().el)

  addAll: ->
    @$fileList.empty()
    @files.each(@addOne, @)

  addMessage: (message) ->
    view = new MessageView({model: message})
    @$messageList.append(view.render().el)
