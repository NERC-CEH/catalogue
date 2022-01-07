define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  defaults:
    versions: []
    coupledResources: []
    containsOperations:[]