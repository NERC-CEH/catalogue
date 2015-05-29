define [
  'backbone'
  'underscore'
], (Backbone, _) -> Backbone.View.extend

  className: 'row'

  events:
    'click button.add':    'add'
    'keydown input': 'addEnter'
    'click button.remove': 'remove'
    'change':              'modify'

  initialize: (options) ->
    @index = if @model.collection then @model.collection.indexOf @model else 'Add'

    if options.collection
      @collection = options.collection

    do @render

  render: ->
    @$el.html @template _.extend index: @index, @model.attributes
    return @

  add: ->
    if @collection
      @collection.add @model.clone(), at: 0
      @model = new @ModelType()
      do @render
      @$('input:first').focus()

  addEnter: (event) ->
    if event.keyCode == 13
      @modify event
      do @add

  remove: ->
    if @model.collection
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
