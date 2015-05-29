define [
  'cs!models/editor/String'
  'cs!views/editor/ParentView'
  'tpl!templates/editor/AlternateTitles.tpl'
  'cs!views/editor/AlternateTitlesItemView'
], (String, ParentView, template, AlternateTitlesItemView) -> ParentView.extend
  template:       template
  ModelType:      String
  modelAttribute: 'alternateTitles'
  ChildView:      AlternateTitlesItemView
