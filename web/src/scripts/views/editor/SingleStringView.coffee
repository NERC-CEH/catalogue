define [
  'backbone'
  'tpl!templates/editor/SingleString.tpl'
], (Backbone, template) -> Backbone.View.extend

  render: ->
    @$el.html template
      identifier: @modelAttribute
      label: @label
      helpText: @helpText
    @