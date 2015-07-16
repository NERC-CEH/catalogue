define [
  'cs!views/editor/InputView'
  'tpl!templates/editor/ResourceStatus.tpl'
], (InputView, template) -> InputView.extend

  template: template

  render: ->
    InputView.prototype.render.apply @
    @$('select').val @model.get @data.modelAttribute
    @