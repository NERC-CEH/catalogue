define [
  'backbone'
  'underscore'
], (Backbone, _) -> Backbone.Model.extend

  defaults:
    address: {}

  toJSON: ->
    if _.isEmpty @get 'address'
      return _.omit @attributes, 'address'
    else
      return _.clone @attributes