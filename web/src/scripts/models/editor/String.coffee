define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  defaults:
    value: ''

  toJSON: ->
    @get 'value'

