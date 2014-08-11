define [
  'backbone'
  'cs!models/OnlineResourceLayer'
], (Backbone, OnlineResourceLayer) -> Backbone.Collection.extend
  model: (attr, options) ->
    return new OnlineResourceLayer attr,options if attr.onlineResource
