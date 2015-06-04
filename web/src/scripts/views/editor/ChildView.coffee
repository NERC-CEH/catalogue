define [
  'backbone'
  'underscore'
], (Backbone, _) -> Backbone.View.extend

  className: 'row'

  events:
    'click button.add':    'add'
    'keydown input':       'addEnter'
    'click button.remove': 'delete'
    'change':              'modify'

  initialize: ->
    @index = if @model.collection then @model.collection.indexOf @model else 'Add'
    do @render

  render: ->
    @$el.html @template _.extend index: @index, @model.attributes
    @

  add: ->
    @trigger 'add', @model
    @model = new @ModelType()
    do @render
    @$('input:first').focus()

  addEnter: (event) ->
    if @index == 'Add' and event.keyCode == 13
      @modify event
      do @add

  delete: ->
    @model.collection.remove @model
    do @remove

  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()

    if value
      @model.set name, value
    else
      @model.unset name
