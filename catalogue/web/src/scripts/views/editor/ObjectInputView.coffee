define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.View.extend

  events:
    'change': 'modify'

  initialize: (options) ->
    @data = options
    @listenTo @model, 'change', @render
    do @render

  render: ->
    @$el.html @template data:  _.extend {}, @data, @model.attributes
    @

  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()

    if not value
      @model.unset name
    else
      @model.set name, value
