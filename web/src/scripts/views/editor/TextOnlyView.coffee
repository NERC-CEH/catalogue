define [
  'underscore'
  'cs!views/editor/SingleView'
  'tpl!templates/editor/TextOnly.tpl'
], (_, SingleView, template) -> SingleView.extend

  template: template

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    do @render

  render: ->
    SingleView.prototype.render.apply @
    @$('.dataentry').append @template data: _.extend {}, @data, value: @model.get @data.modelAttribute
