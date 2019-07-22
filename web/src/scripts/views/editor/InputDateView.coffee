define [
  'cs!views/editor/InputView'
  'tpl!templates/editor/InputDate.tpl'
  'jquery-ui/datepicker'
], (InputView, template) -> InputView.extend

  template: template

  render: ->
    InputView.prototype.render.apply @
    @$('input').datepicker dateFormat: "yy-mm-dd"
    @
