define [
  'backbone'
  'tpl!templates/upload/simple/File.tpl'
], (Backbone, template) -> Backbone.View.extend

  tagName: 'li'

  template: template

  events:
    'change input': 'select' 

  initialize: ->
    @listenTo(@model, 'sync', @remove)
    @listenTo(@model, 'change', @render)

  select: ->
    previous = @model.get('toDelete')
    @model.set('toDelete', !previous)

  render: ->
    @$el.html(@template(@model.attributes))
    @
