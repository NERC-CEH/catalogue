define [
  'backbone'
], (Backbone) -> Backbone.View.extend

  events:
    'change': 'modify'

  initialize: ->
    @listenTo @model, 'change', @render
    do @render

  render: ->
    @$el.html @template @model.attribute
    @

  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()

    if not value
      @model.unset name
    else
      @model.set name, value
