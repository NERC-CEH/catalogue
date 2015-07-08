define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  initialize: (options) ->
    @set 'value', options

  defaults:
    value: ''

  toJSON: ->
    @get 'value'

