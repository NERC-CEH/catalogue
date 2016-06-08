define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/ServiceOperation.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select.operationName').val @model.get 'operationName'
    @$('select.platform').val @model.get 'platform'