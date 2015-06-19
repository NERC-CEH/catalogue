define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/Textarea.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  initialize: (options) ->
    @modelAttribute = options.modelAttribute
    @rows = if options.rows then options.rows else 13
    do @render

  render: ->
    @$el.html template
      identifier: @modelAttribute
      value: @model.get 'value'
      rows: @rows
    @
