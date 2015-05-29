define [
  'backbone'
  'cs!views/editor/ParentView'
  'tpl!templates/editor/AlternateTitles.tpl'
  'cs!views/editor/AlternateTitlesItemView'
], (Backbone, ParentView, template, AlternateTitlesItemView) -> ParentView.extend
  template:       template
  ModelType:      Backbone.Model.extend
    defaults:
      value: ''
  modelAttribute: 'alternateTitles'
  ChildView:      AlternateTitlesItemView
