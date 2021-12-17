define [
  'underscore'
  'cs!views/editor/SingleView'
  'tpl!templates/service-agreement/TextOnly.tpl'
], (_, SingleView, template) -> SingleView.extend 

  template: template

  className: 'component component--textonly'

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    do @render

  render: ->
    SingleView.prototype.render.apply @
    @$('.dataentry').append @template data: _.extend {}, @data, value: @model.get