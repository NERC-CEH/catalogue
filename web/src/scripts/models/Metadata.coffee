define [
  'underscore'
  'backbone'
  'documentmodel'
], (_, Backbone) -> Backbone.DocumentModel.extend
  urlRoot: '/documents'