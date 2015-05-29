define [
  'cs!models/editor/String'
  'cs!views/editor/ChildView'
  'tpl!templates/editor/AlternateTitlesItem.tpl'
], (String, ChildView, template) -> ChildView.extend
  template: template
  ModelType: String