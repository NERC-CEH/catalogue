define [
  'cs!views/editor/ChildView'
  'tpl!templates/editor/ResourceIdentifiersItem.tpl'
  'cs!models/editor/ResourceIdentifier'
], (ChildView, template, ResourceIdentifier) -> ChildView.extend
  template:  template
  ModelType: ResourceIdentifier