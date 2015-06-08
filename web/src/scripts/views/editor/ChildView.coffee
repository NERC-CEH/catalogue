define [
  'backbone'
  'tpl!templates/editor/Child.tpl'
], (Backbone, template) -> Backbone.View.extend

  className: 'row'

  events:
    'click button.add':    'add'
    'keydown input':       'addEnter'
    'click button.remove': 'delete'

  initialize: (options) ->
    @index = if @model.collection then @model.collection.indexOf @model else 'Add'
    @ModelType = options.ModelType
    do @render
    new options.ObjectInputView
      el: @$('.dataentry')
      model: @model

  render: ->
    @$el.html template index: @index
    @

  add: ->
    @trigger 'add', @model.clone()
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
