define [
  'backbone'
  'underscore'
], (Backbone, _) -> Backbone.Model.extend

  defaults:
    constraints: {}

  toJSON: ->
    if _.isEmpty @get 'constraints'
      return _.omit @attributes, 'constraints'
    else
      return _.clone @attributes