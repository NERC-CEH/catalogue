define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/InspireTheme.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select.theme').val @model.get 'theme'
    @$('select.conformity').val @model.get 'conformity'
    @