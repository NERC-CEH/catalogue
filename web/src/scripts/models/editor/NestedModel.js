// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * DS207: Consider shorter variations of null checks
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'backbone',
  'cs!collections/Positionable'
], function(_, Backbone, Positionable) { let NestedModel;
return NestedModel = Backbone.Model.extend({
  
  /*
  Return a collection which is bound to an array attribute of the given model.
  The collection will by default create instances of `NestedModel` and be of
  type `Positionable`. Any changes to the returned collection will be reflected
  in the models specified attribute.
  */
  getRelatedCollection(attr, model, collection) {
    if (model == null) { model = NestedModel; }
    if (collection == null) { collection = Positionable; }
    collection = new collection(this.get(attr),
      {model});

    this.listenTo(collection, 'add remove change position', () => {
      return this.set(attr, collection.toJSON());
    });

    return collection;
  },

  /*
  Return a model representation for an attribute on this model. Any changes to
  the returned model will be automatically reflected on this models attribute.
  */
  getRelated(attr, model) {
    if (model == null) { model = NestedModel; }
    model = new model(this.get(attr));
    this.listenTo(model, 'change', () => this.set(attr, model.toJSON()));
    return model;
  }
});
 });
