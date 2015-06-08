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
    @

  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()
    @dataEntry.set name, value
    console.log "name: #{name}, value: #{value}"
    if @dataEntry.isEmpty()
      @model.unset @modelAttribute
    else
      @model.set @modelAttribute, @dataEntry.clone()
