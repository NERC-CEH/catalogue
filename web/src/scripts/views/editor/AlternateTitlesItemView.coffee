define [
  'backbone'
  'underscore'
  'cs!views/editor/ChildView'
  'tpl!templates/editor/AlternateTitlesItem.tpl'
], (Backbone, _, ChildView, template) -> ChildView.extend
  template: template
  ModelType: Backbone.Model.extend
    defaults:
      value: ''