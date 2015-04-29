define [
  'backbone'
  'tpl!templates/editor/ResourceType.tpl'
], (Backbone, template) -> Backbone.View.extend

  render: ->
    @$el.html template
    console.log 'resource type render'
    return @
