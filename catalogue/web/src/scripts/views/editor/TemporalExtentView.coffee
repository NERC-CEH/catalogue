define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/TemporalExtent.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('input').datepicker dateFormat: "yy-mm-dd"
    @