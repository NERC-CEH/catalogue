define [
  'cs!views/editor/InputView'
  'tpl!templates/editor/Select.tpl'
  'tpl!templates/editor/Option.tpl'
], (InputView, template, optionTemplate) -> InputView.extend

  template: template

  render: ->
    InputView.prototype.render.apply @
    $select = @$('select')
    $select.append optionTemplate
      value: ''
      label: "- Select #{@data.label} -"
    _.each @data.options, (option) ->
      $select.append optionTemplate option

    $select.val @model.get @data.modelAttribute
    @