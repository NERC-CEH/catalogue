import Backbone from 'backbone'
import { Positionable } from '../collections'

export const NestedModel = Backbone.Model.extend({

    /*
    Return a collection which is bound to an array attribute of the given model.
    The collection will by default create instances of `NestedModel` and be of
    type `Positionable`. Any changes to the returned collection will be reflected
    in the models specified attribute.
    */
    getRelatedCollection (attr, model, collection) {
        collection = new Positionable(this.get(attr), { model })

        this.listenTo(collection, 'add remove change position', () => { this.set(attr, collection.toJSON()) })

        return collection
    },

    /*
    Return a model representation for an attribute on this model. Any changes to
    the returned model will be automatically reflected on this models attribute.
    */
    getRelated (attr, model) {
        model = new NestedModel(this.get(attr))
        this.listenTo(model, 'change', () => this.set(attr, model.toJSON()))
        return model
    }
})
