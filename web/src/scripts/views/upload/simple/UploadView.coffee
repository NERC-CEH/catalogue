define [
  'backbone'
  'cs!models/upload/simple/File'
], (Backbone, File) -> Backbone.View.extend

  initialize: (options) ->
    @files = options.files
    @messages = options.messages

    # Treating Dropzone as a global variable
    # Dropzone 'required' in Main.coffee, does not work if 'required' here! 
    dropzone = new Dropzone(@el, {url: @url})
    dropzone.on('success', (file) =>
      @messages.add(new Backbone.Model(message: "Uploaded #{file.name}", type: 'info'))
      model = new File({name: file.name})
      console.log(model)
      @files.add(model)
    )
    dropzone.on('error', (file, errorMessage) =>
      @messages.add(new Backbone.Model(message: "#{file.name} - #{errorMessage}", type: 'error'))
    )