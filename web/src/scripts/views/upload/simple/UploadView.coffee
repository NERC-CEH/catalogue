define [
  'backbone'
  'dropzone'
  'cs!models/upload/simple/File'
], (Backbone, Dropzone, File) -> Backbone.View.extend

  initialize: (options) ->
    # Prevent Dropzone auto discovery
    Dropzone.simpleUploadDropzone = false
    # Enable Dropzone CSS styling
    @$el.addClass('dropzone')

    dropzone = new Dropzone(@el)
    dropzone.on('success', (file) ->
      options.messages.add(new Backbone.Model(message: "Uploaded: #{file.name}", type: 'info'))
      options.files.add(new File({name: file.name}))
    )
    dropzone.on('error', (file, errorMessage) ->
      options.messages.add(new Backbone.Model(errorMessage))
    )
    dropzone.on('complete', (file) ->
      dropzone.removeFile(file)
    )