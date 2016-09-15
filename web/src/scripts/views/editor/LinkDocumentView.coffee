define [
  'backbone'
  'tpl!templates/editor/LinkDocument.tpl'
],
(Backbone, template) -> Backbone.View.extend

  events:
    'click button': 'selected'

  render: ->
    @$el.html template  @model.attributes
    @

  selected: (event) ->
    @model.trigger 'selected', @model.get 'identifier'