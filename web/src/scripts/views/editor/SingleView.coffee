define [
  'backbone'
  'tpl!templates/editor/Single.tpl'
], (Backbone, template) -> Backbone.View.extend

  className: 'component'

  initialize: (options) ->
    @data = options

  render: ->
    @$el.html template  data: @data
    @