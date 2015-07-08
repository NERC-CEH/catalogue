define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/Textarea.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  initialize: (options) ->
    unless options.rows?
      options.rows = 13

    ObjectInputView.prototype.initialize.call @, options
