define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/ConformanceResult.tpl'
  'jquery-ui/datepicker'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('input.date').datepicker dateFormat: "yy-mm-dd"
    @$('select').val @model.get 'dateType'
    @$("input[name='pass#{@data.index}'][value='#{@model.get('pass')}']")
      .prop 'checked', true
    @
