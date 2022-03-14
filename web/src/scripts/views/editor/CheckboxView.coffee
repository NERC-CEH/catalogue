define [
  'cs!views/editor/InputView'
  'tpl!templates/editor/Checkbox.tpl'
], (InputView, template) -> InputView.extend

  template: template

  render: ->
    InputView.prototype.render.apply @
    @$('[type="checkbox"]').prop 'checked', @model.get @data.modelAttribute
    @

  modify: (event) ->
    $target = $ event.target
    name = $target.data 'name'
    value = $target.prop 'checked'
    @model.set name, value