define [
  'underscore'
  'backbone'
  'tpl!templates/editor/Child.tpl'
], (_, Backbone, template) -> Backbone.View.extend

  className: 'row'

  events:
    'click button.add':    'add'
    'click button.remove': 'delete'

  initialize: (options) ->
    @index = if @model.collection then @model.collection.indexOf @model else 'Add'
    do @render
    console.log "ChildView: #{JSON.stringify @model.toJSON()}"
    new options.ObjectInputView
      el: @$('.dataentry')
      model: @model

  render: ->
    @$el.html template index: @index
    @

  add: ->
    @trigger 'add', @model.clone()
    _.each @model.keys(), (key) =>
      @model.unset key

  delete: ->
    @model.collection.remove @model
    do @remove
