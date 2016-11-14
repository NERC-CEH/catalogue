define [
  'underscore'
  'cs!collections/Positionable'
  'cs!views/editor/SingleView'
  'cs!views/editor/ChildView'
  'tpl!templates/editor/Parent.tpl'
], (_, Positionable, SingleView, ChildView, template) -> SingleView.extend

  events:
    'click button.add': 'add'

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    @collection = new Positionable [], model: @data.ModelType

    @listenTo @collection, 'add', @addOne
    @listenTo @collection, 'reset', @addAll
    @listenTo @collection, 'add remove change position', @updateModel
    @listenTo @model, 'sync', @updateCollection

    do @render
    @$attach = @$(".existing")
    @collection.reset @getModelData()

    if @data.multiline
      @$el.addClass 'multiline'

    @$attach.sortable
      start: (event, ui) =>
        @_oldPosition = ui.item.index()
      update: (event, ui) =>
        @collection.position @_oldPosition, ui.item.index()

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

  getModelData: ->
    model = @model.attributes
    path = @data.modelAttribute.split '.'
    while path.length >= 2
      model = model[path.shift()] or {}

    return model[path[0]] or []

  updateModel: ->
    path = @data.modelAttribute.split '.'
    data = @collection.toJSON()

    while path.length > 0
      oldData = data
      data = {}
      data[path.pop()] = oldData
    @model.set data

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

  show: ->
    SingleView.prototype.show.apply @
    @collection.trigger 'visible'

  hide: ->
    SingleView.prototype.hide.apply @
    @collection.trigger 'hidden'
