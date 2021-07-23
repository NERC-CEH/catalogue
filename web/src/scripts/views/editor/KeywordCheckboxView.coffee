define [
  'backbone'
  'tpl!templates/editor/KeywordCheckbox.tpl'
], (Backbone, template) -> Backbone.View.extend

  tagName: 'li'

  template: template

  events:
    'change input': 'select'

  initialize: ->
    console.log("keyword checkbox initialize")
    @listenTo(@model, 'sync', @remove)
    @listenTo(@model, 'change', @render)

  select: ->
    console.log("select checkbox")
    previous = @model.get('toSearch')
    @model.set('toSearch', !previous)

  render: ->
    console.log("render checkbox")
    @$el.html(@template(@model.attributes))
    @
