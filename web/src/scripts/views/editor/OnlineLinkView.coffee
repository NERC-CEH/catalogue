define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/OnlineLink.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
