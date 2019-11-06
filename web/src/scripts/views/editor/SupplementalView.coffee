define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/Supplemental.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select.function').val @model.get 'function'
    @
