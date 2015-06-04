define [
  'backbone'
  'tpl!templates/editor/Single.tpl'
], (Backbone, template) -> Backbone.View.extend

  initialize: (options) ->
    @modelAttribute = options.modelAttribute
    @label = options.label
    @helpText = options.helpText

  render: ->
    @$el.html template
      identifier: @modelAttribute
      label: @label
      helpText: @helpText
    @