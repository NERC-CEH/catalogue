define [
  'backbone'
  'tpl!templates/editor/KeywordCheckbox.tpl'
], (Backbone, template) -> Backbone.View.extend

  template: template

  events:
    'change input': 'select'

  initialize: ->
    @listenTo(@model, 'sync', @remove)
    @listenTo(@model, 'change', @render)

  select: ->
    console.log("select")
    previous = @model.get('toSearch')
    @model.set('toSearch', !previous)

  render: ->
    @$el.html(@template(@model.attributes))
    @
