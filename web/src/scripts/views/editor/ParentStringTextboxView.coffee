define [
  'cs!views/editor/ParentStringView'
  'tpl!templates/editor/MultiStringTextbox.tpl'
], (ParentStringView, childTemplate) -> ParentStringView.extend

  childTemplate: childTemplate

  initialize: (options) ->
    ParentStringView.prototype.initialize.call @, options