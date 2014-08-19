define [
  'underscore'
  'backbone'
  'cs!collections/Layers'
], (_, Backbone, Layers) -> Backbone.Model.extend

  defaults:
    layers: new Layers

  getLayers: -> @get 'layers'