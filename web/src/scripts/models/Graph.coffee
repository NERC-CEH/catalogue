define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend
  url: "/graph"
  initialize: (id) ->
    @id = id
    @url = "/graph/#{id}"
    @urlRoot = "/graph/#{id}"
    do @fetch