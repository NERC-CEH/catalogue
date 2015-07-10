define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/DatasetReferenceDate.tpl'
  'jquery-ui/datepicker'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('input').datepicker dateFormat: "yy-mm-dd"
    @
