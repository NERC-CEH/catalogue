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

    @listenTo @collection, 'add', @addOne
    @listenTo @collection, 'reset', @addAll
    @listenTo @collection, 'add remove change', @updateModel
    @listenTo @model, 'sync', @updateCollection

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
    @$attach.append view.el

  addAll: ->
    @$attach.html('')
    @collection.each @addOne, @

  add: ->
    @collection.add new @data.ModelType

  updateModel: ->
    @model.set @data.modelAttribute, @collection.toJSON()

  updateCollection: (model) ->
    if model.hasChanged @data.modelAttribute
      updated = model.get @data.modelAttribute

      collectionLength = @collection.length
      # Update existing models
      _.chain(updated)
        .first(collectionLength)
        .each((update, index) =>
          @collection
            .at(index)
            .set(update)
        )
      # Add new models
      _.chain(updated)
        .rest(collectionLength)
        .each ((update) =>
          @collection.add update
        )
      # Remove models not in updated
      @collection.remove(@collection.rest(updated.length))
