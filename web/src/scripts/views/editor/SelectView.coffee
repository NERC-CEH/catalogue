define [
  'underscore'
  'cs!views/editor/InputView'
  'tpl!templates/editor/Select.tpl'
], (_, InputView, template) -> InputView.extend

  template: template

  optionTemplate: _.template('<option value="<%= value %>"><%= label %></option>')

  render: ->
    InputView.prototype.render.apply @
    $select = @$ 'select'
    @model.options.forEach (option) =>
      $select.append @optionTemplate option
    $select.val @model.get @data.modelAttribute
    @