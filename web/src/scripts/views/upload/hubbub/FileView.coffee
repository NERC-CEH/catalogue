define [
  'backbone'
  'tpl!templates/upload/hubbub/FileRow.tpl'
], (Backbone, template) -> Backbone.View.extend

  template: template

  initialize: ->
    @listenTo(@model, 'change', @render)

  render: ->
    @$el.html(@template(@model.attributes))
    @