define [
  'backbone'
  'dropzone'
  'cs!models/upload/simple/File'
], (Backbone, Dropzone, File) -> Backbone.View.extend

  initialize: (options) ->
    @files = options.files  
    @messages = options.messages

    dropzone = Dropzone.forElement(@el)
    dropzone.on('success', (file) =>
      @messages.add(new Backbone.Model(message: "Uploaded: #{file.name}", type: 'info'))
      @files.add(new File({name: file.name}))
    )
    dropzone.on('error', (file, errorMessage) =>
      @messages.add(new Backbone.Model(errorMessage))
    )
    dropzone.on('complete', (file) =>
      dropzone.removeFile(file)
    )