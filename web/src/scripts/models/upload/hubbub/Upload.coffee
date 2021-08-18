define [
  'backbone'
  'jquery'
], (Backbone, $) -> Backbone.Model.extend

  urlRoot:
    "/upload"

  defaults:
    datastorePage: 1
    dropboxPage: 1
    metadataPage: 1
    datastoreSize: 20
    dropboxSize: 20
    metadataSize: 20
