define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend

  defaults:
    toDelete: false

  idAttribute: 'name'
