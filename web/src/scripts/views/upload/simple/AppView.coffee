define [
  'jquery'
  'underscore'
  'backbone'
  'dropzone'
  'cs!collections/upload/simple/Files'
  'cs!views/upload/simple/FileView'
], ($, _, Backbone, Dropzone, Files, FileView) -> Backbone.View.extend

  initialize: (options) ->
    @files = new Files
      url: options.url

    @$fileList = @$('#files')
    @$input = @$('#simple-upload-dropzone')

    @listenTo(@files, 'add', @addOne)
    @listenTo(@files, 'reset', @addAll)

    @files.fetch
      reset:true

  addOne: (file) ->
    view = new FileView({model: file})
    @$fileList.append(view.render().el)

  addAll: ->
    @$fileList.empty()
    @files.each(@.addOne, @)
