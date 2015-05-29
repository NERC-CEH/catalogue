define [
  'backbone'
  'underscore'
], (Backbone, _) -> Backbone.Model.extend

  defaults:
    individualName: ''
    organisationName: ''
    email: ''
    role: ''
    address: {}

  toJSON: ->
    if _.isEmpty @get 'address'
      return _.omit @attributes, 'address'
    else
      return @attributes