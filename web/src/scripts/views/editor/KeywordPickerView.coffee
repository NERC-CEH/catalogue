define [
  'backbone'
  'tpl!templates/editor/KeywordPicker.tpl'
], (Backbone, template) -> Backbone.View.extend

  template: template

  events:
    'click .add': (event) -> @addPredefined(event)

  initialize: (options) ->
    @addPredefined=options.addPredefined
    @render()
    @

  render: ->
      @$el.html(@template())
      @
