define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/ModelQA.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template


  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select.type').val @model.get 'type'
    @
 