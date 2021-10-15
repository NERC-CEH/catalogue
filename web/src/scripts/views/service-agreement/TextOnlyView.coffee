define [
  'cs!views/editor/SingleView'
  'tpl!templates/service-agreement/TextOnly.tpl'
], (SingleView, template) -> SingleView.extend

  template: template

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    do @render

  render: ->
    SingleView.prototype.render.apply @
    @$('.dataentry').append(@template(@data))
