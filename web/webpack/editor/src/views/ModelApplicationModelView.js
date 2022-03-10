define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/ModelApplicationModel.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select').val @model.get 'applicationScale'
    @