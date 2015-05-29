define [
  'backbone'
], (Backbone) -> Backbone.View.extend

  initialize: ->
    @collection = new Backbone.Collection @model.get(@modelAttribute), model: @ModelType
    @listenTo @collection, 'add remove change', @updateModel
    @insertionPoint = "##{@modelAttribute}"
    @newPoint = "##{@modelAttribute}AddNew"

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
