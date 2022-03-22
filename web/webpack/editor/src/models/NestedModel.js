import Backbone from 'backbone'
import { Positionable } from '../collections'

export var NestedModel = Backbone.Model.extend({

  /*
  Return a collection which is bound to an array attribute of the given model.
  The collection will by default create instances of `NestedModel` and be of
  type `Positionable`. Any changes to the returned collection will be reflected
  in the models specified attribute.
  */
  getRelatedCollection (attr, model, collection) {
    if (model == null) { model = NestedModel }
    if (collection == null) { collection = Positionable }
    collection = new Backbone.Collection(this.get(attr),
      { model })

    this.listenTo(collection, 'add remove change position', () => {
      return this.set(attr, collection.toJSON())
    })

    return collection
  },

  /*
  Return a model representation for an attribute on this model. Any changes to
  the returned model will be automatically reflected on this models attribute.
  */
  getRelated (attr, model) {
    if (model == null) { model = NestedModel }
    model = new Backbone.Model(this.get(attr))
    this.listenTo(model, 'change', () => this.set(attr, model.toJSON()))
    return model
  }
})
