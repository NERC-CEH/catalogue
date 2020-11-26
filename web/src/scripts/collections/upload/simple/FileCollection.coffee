define [
  'backbone'
  'cs!models/upload/simple/File'
], (Backbone, File) -> Backbone.Collection.extend
    model: File

    comparator: 'name'

    initialize: (options) ->
      @url = options.url
