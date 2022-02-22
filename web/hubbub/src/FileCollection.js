define [
  'backbone'
  'cs!models/upload/hubbub/File'
], (Backbone, File) -> Backbone.Collection.extend
    model: File
