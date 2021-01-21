define [
  'backbone'
  'tpl!templates/editor/KeywordPicker.tpl'
], (Backbone, template) -> Backbone.View.extend

  template: template

  initialize: ->

  render: ->
      @$el.html(@template(@model.attributes))
      @