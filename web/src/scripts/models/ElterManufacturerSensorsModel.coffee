define [
  'backbone'
], (Backbone) -> Backbone.Model.extend
  initialize: (id) ->
    @urlRoot = '/elter/sensors/' + id