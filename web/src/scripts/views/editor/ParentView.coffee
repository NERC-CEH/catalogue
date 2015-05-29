define [
  'backbone'
  'underscore'
], (Backbone, _) -> Backbone.View.extend

  initialize: ->
    data = @model.get(@modelAttribute)
    # detect an array of Strings and create an object that can be turned into a Model
    if _.isArray(data) and _.isString _.first data
      data = data.map (value) ->
        value: value

    @collection = new Backbone.Collection data, model: @ModelType
    @listenTo @collection, 'add remove change', @updateModel
    @insertionPoint = "##{@modelAttribute}"
    @newPoint = "##{@modelAttribute}AddNew"
    do @render

  addOne: (model) ->
    view = new @ChildView
      model: model
    @$(@insertionPoint).append view.render().el

  addAll: ->
    @$(@insertionPoint).html('')
    @collection.each @addOne, @

  render: ->
    @$el.html @template
    do @addAll

    newOne = new @ChildView
      el: @$(@newPoint)
      model: new @ModelType()
      collection: @collection
    do newOne.render

  updateModel: ->
    if @collection.length > 0
      @model.set @modelAttribute, @collection
    else
      @model.unset @modelAttribute
    do @addAll
