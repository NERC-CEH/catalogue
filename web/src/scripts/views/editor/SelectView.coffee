define [
  'underscore'
  'cs!views/editor/InputView'
  'tpl!templates/editor/Select.tpl'
], (_, InputView, template) -> InputView.extend

  template: template

  optionTemplate: _.template('<option value="<%= value %>"><%= label %></option>')

  initialize: (options) ->
    @options = options.options || []
    InputView.prototype.initialize.call @, options

  render: ->
    InputView.prototype.render.apply @
    $select = @$ 'select'
    @options.forEach (option) =>
      $select.append @optionTemplate option
    $select.val @model.get @data.modelAttribute
    @