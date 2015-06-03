define [
  'backbone'
  'tpl!templates/editor/Input.tpl'
], (Backbone, template) -> Backbone.View.extend

  events:
    'change': 'modify'

  initialize: (options) ->
    @modelAttribute = options.modelAttribute
    do @render

  render: ->
    @$el.html template
      identifier: @modelAttribute
      value: @model.get @modelAttribute
    @

  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()

    if value
      @model.set name, value
    else
      @model.unset name