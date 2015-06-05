define [
  'backbone'
  'tpl!templates/editor/Child.tpl'
], (Backbone, template) -> Backbone.View.extend

  className: 'row'

  events:
    'click button.add':    'add'
    'keydown input':       'addEnter'
    'click button.remove': 'delete'
    'change':              'modify'

  initialize: (options) ->
    @index = if @model.collection then @model.collection.indexOf @model else 'Add'
    do @render
    console.log "child View"
    console.dir options
    new options.ObjectInputView
      el: @$('.dataentry')
      model: @model

  render: ->
    @$el.html template index: @index
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
