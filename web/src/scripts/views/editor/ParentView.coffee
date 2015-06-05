define [
  'underscore'
  'backbone'
  'cs!views/editor/SingleView'
  'cs!views/editor/ChildView'
  'tpl!templates/editor/Parent.tpl'
], (_, Backbone, SingleView, ChildView, template) -> SingleView.extend

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    @ObjectInputView = options.ObjectInputView

    data = if @model.has @modelAttribute then @model.get @modelAttribute else []
    # detect an array of Strings and create an object that can be turned into a Model
    if _.isArray(data) and _.isString _.first data
      data = data.map (value) ->
        value: value

    @collection = new Backbone.Collection [], model: options.ModelType
    @model.set @modelAttribute, @collection, silent: true

    @listenTo @collection, 'add', @addOne
    @listenTo @collection, 'reset', @addAll
    @listenTo @collection, 'add remove change', @saveRequired

    do @render
    @$attach = @$(".existing")
    @collection.reset data

    addView = new ChildView
      el: @$('.addNew')
      model: new options.ModelType
      ObjectInputView: @ObjectInputView

    @listenTo addView, 'add', (model) ->
      @collection.add model

  render: ->
    @$el.html template
      identifier: @modelAttribute
      label: @label
      helpText: @helpText
    @

  addOne: (model) ->
    view = new ChildView
      model: model
      ObjectInputView: @ObjectInputView
    @$attach.prepend view.el

  addAll: ->
    @$attach.html('')
    @collection.each @addOne, @

  saveRequired: ->
    @model.trigger 'save:required'
