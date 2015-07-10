define [
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/OnlineResource.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select').val @model.get 'function'
    @