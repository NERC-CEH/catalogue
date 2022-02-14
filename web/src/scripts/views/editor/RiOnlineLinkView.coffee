define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/RiOnlineLink.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
