define [
  'underscore'
  'backbone'
  'cs!views/editor/SingleView'
  'cs!views/editor/ChildView'
  'tpl!templates/editor/Parent.tpl'
], (_, Backbone, SingleView, ChildView, template) -> SingleView.extend

  events:
    'click button.add': 'add'

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    modelData = if @model.has @data.modelAttribute then @model.get @data.modelAttribute else []
    @collection = new Backbone.Collection [], model: @data.ModelType
    @model.set @data.modelAttribute, @collection, silent: true

    @listenTo @collection, 'add', @addOne
    @listenTo @collection, 'reset', @addAll
    @listenTo @collection, 'add remove change', @saveRequired
    @listenTo @model, 'sync', (model) ->
      serverUpdate = model.get @data.modelAttribute
      @collection.reset serverUpdate
      # If the server has modified a repeating element the model will be returned with an array not a collection
      if _.isArray serverUpdate
        @model.set @data.modelAttribute, @collection

    do @render
    @$attach = @$(".existing")
    @collection.reset modelData

    if @data.multiline
      @$el.addClass 'multiline'

  render: ->
    @$el.html template data: @data
    @

  addOne: (model) ->
    view = new ChildView _.extend {}, @data,
      model: model
    @$attach.prepend view.el

  addAll: ->
    @$attach.html('')
    @collection.each @addOne, @

  add: ->
    @collection.add new @data.ModelType

  saveRequired: ->
    @model.trigger 'save:required'
