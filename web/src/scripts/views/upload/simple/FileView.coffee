define [
  'jquery'
  'underscore'
  'backbone'
  'tpl!templates/upload/simple/File.tpl'
], ($, _, Backbone, template) -> Backbone.View.extend

  tagName: 'li'

  template: template

  events:
    'click .destroy': 'clear'

  initialize: ->
    @listenTo(@model, 'sync', @remove);

  clear: ->
    if confirm("Delete file: #{@model.get('name')}?")
      @model.destroy
        success: (model, response) ->
          $('#message').html(response.message) if response.message?
        error: (model, response) ->
          $('#message').html(response.message) if response.message?

  render: ->
    @$el.html(@template(@model.attributes))
    @