define [
  'backbone'
  'underscore'
], (Backbone, _) -> Backbone.View.extend

  initialize: ->

    data = @model.get @modelAttribute
    # detect an array of Strings and create an object that can be turned into a Model
    if _.isArray(data) and _.isString _.first data
      data = data.map (value) ->
        value: value

    @collection = new Backbone.Collection [], model: @ModelType
    @model.set @modelAttribute, @collection, silent: true

    @listenTo @collection, 'add', @addOne
    @listenTo @collection, 'reset', @addAll

    do @render
    @$attach = @$("##{@modelAttribute}")
    @collection.reset data

    addView = new @ChildView
      el: @$("##{@modelAttribute}AddNew")
      model: new @ModelType()

    @listenTo addView, 'add', (model) ->
      @collection.add model

  addOne: (model) ->
    view = new @ChildView
      model: model
    @$attach.prepend view.el

  addAll: ->
    @$attach.html('')
    @collection.each @addOne, @

  render: ->
    @$el.html @template
