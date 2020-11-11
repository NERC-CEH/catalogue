define [
  'backbone'
  'cs!models/upload/simple/File'
], (Backbone, File) -> Backbone.Collection.extend
    model: File

    initialize: (options) ->
      @url = options.url

    parse: (response) ->
      response.files