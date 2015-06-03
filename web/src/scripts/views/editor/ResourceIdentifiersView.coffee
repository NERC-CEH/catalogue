define [
  'underscore'
  'cs!views/editor/ParentView'
  'tpl!templates/editor/ResourceIdentifiers.tpl'
  'cs!models/editor/ResourceIdentifier'
  'cs!views/editor/ResourceIdentifiersItemView'
], (_, ParentView, template, ResourceIdentifier, ResourceIdentifiersItemView) -> ParentView.extend
  template:       template
  ModelType:      ResourceIdentifier
  modelAttribute: 'resourceIdentifiers'
  ChildView:      ResourceIdentifiersItemView

  events: ->
    _.extend 'click #resourceIdentifiersAddNewEidc': @generate, ParentView.prototype.events

  generate: ->
    @collection.add new ResourceIdentifier generateCeh: true
