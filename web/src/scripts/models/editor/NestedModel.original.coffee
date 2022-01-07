define [
  'underscore'
  'backbone'
  'cs!collections/Positionable'
], (_, Backbone, Positionable) -> NestedModel = Backbone.Model.extend
  
  ###
  Return a collection which is bound to an array attribute of the given model.
  The collection will by default create instances of `NestedModel` and be of
  type `Positionable`. Any changes to the returned collection will be reflected
  in the models specified attribute.
  ###
  getRelatedCollection: (attr, model = NestedModel, collection = Positionable) ->
    collection = new collection @get(attr),
      model: model

    @listenTo collection, 'add remove change position', =>
      @set attr, collection.toJSON()

    return collection

  ###
  Return a model representation for an attribute on this model. Any changes to
  the returned model will be automatically reflected on this models attribute.
  ###
  getRelated: (attr, model = NestedModel) ->
    model = new model @get(attr)
    @listenTo model, 'change', => @set attr, model.toJSON()
    return model
