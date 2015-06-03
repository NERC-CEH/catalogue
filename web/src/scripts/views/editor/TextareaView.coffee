define [
  'cs!views/editor/InputView'
  'tpl!templates/editor/Textarea.tpl'
], (InputView, template) -> InputView.extend

  initialize: (options) ->
    @modelAttribute = options.modelAttribute
    @rows = if options.rows then options.rows else 13
    do @render

  render: ->
    @$el.html template
      identifier: @modelAttribute
      value: @model.get @modelAttribute
      rows: @rows
    @
