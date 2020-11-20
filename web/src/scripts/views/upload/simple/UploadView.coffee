define [
  'backbone'
  'cs!models/upload/simple/File'
], (Backbone, File) -> Backbone.View.extend
  # Treating Dropzone as a global variable
  # Dropzone 'required' in Main.coffee, does not work if 'required' here! 

  initialize: (options) ->
    @files = options.files  
    @messages = options.messages
    @url = options.url

    dropzone = new Dropzone(@el, {url: @url})
    dropzone.on('success', (file) =>
      @messages.add(new Backbone.Model(message: "Uploaded #{file.name}", type: 'info'))
      @files.add(new File({name: file.name}))
    )
    dropzone.on('error', (file, errorMessage) =>
      @messages.add(new Backbone.Model(message: "#{file.name} - #{errorMessage}", type: 'error'))
    )