define [
  'cs!views/editor/ParentView'
  'tpl!templates/editor/ResourceIdentifiers.tpl'
  'cs!models/editor/ResourceIdentifier'
  'cs!views/editor/ResourceIdentifiersItemView'
], (ParentView, template, ResourceIdentifier, ResourceIdentifiersItemView) -> ParentView.extend
  template:       template
  ModelType:      ResourceIdentifier
  modelAttribute: 'resourceIdentifiers'
  ChildView:      ResourceIdentifiersItemView
