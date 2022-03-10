define [
  'underscore'
  'cs!views/editor/SingleView'
  'tpl!templates/editor/ReadOnly.tpl'
], (_, SingleView, template) -> SingleView.extend

  template: template

  events:
    'change': 'modify'

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    do @render
    @listenTo @model, "change:#{@data.modelAttribute}", @render

  render: ->
    SingleView.prototype.render.apply @
    @$('.dataentry').append @template data: _.extend {}, @data, value: @model.get @data.modelAttribute

  modify: (event) ->
    $target = $ event.target
    name = $target.data 'name'
    value = $target.val()

    if not value
      @model.unset name
    else
      @model.set name, value