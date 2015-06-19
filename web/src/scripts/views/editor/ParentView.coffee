define [
  'underscore'
  'backbone'
  'cs!views/editor/SingleView'
  'cs!views/editor/ChildView'
  'tpl!templates/editor/Parent.tpl'
], (_, Backbone, SingleView, ChildView, template) -> SingleView.extend

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    modelData = if @model.has @data.modelAttribute then @model.get @data.modelAttribute else []

    # detect an array of Strings and create an object that can be turned into a Model
    if _.isArray(modelData) and _.isString _.first modelData
      modelData = modelData.map (value) ->
        value: value

    @collection = new Backbone.Collection [], model: @data.ModelType
    @model.set @data.modelAttribute, @collection, silent: true

    @listenTo @collection, 'add', @addOne
    @listenTo @collection, 'reset', @addAll
    @listenTo @collection, 'add remove change', @saveRequired

    do @render
    @$attach = @$(".existing")
    @collection.reset modelData

    addView = new ChildView _.extend {}, @data,
      el: @$('.addNew')
      model: new @data.ModelType

    @listenTo addView, 'add', (model) ->
      @collection.add model

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

  saveRequired: ->
    @model.trigger 'save:required'
