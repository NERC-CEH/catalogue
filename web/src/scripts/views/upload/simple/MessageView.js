define [
  'backbone'
  'tpl!templates/upload/simple/Message.tpl'
], (Backbone, template) -> Backbone.View.extend

  tagName: 'li'

  template: template

  initialize: ->
    @.listenTo(@model, 'remove', @remove)

  render: ->
    @$el.html(@template(@model.attributes))
    @
