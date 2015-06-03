define [
  'backbone'
], (Backbone) -> Backbone.View.extend

  events:
    'change': 'modify'

  initialize: ->
    @dataEntry = if @model.has @modelAttribute then new @ModelType @model.get @modelAttribute else new @ModelType()
    @model.set @modelAttribute, @dataEntry, silent: true
    do @render

  render: ->
    @$el.html @template @dataEntry.attributes
    return @

  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()

    if value
      @dataEntry.set name, value
      @model.set @modelAttribute, @dataEntry.clone()
    else
      @model.unset @modelAttribute