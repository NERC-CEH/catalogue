define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/Model_Link.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select.webtitle').val @model.get 'title'
    @
