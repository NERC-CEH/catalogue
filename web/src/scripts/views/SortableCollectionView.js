/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'underscore',
  'backbone',
  'jquery-ui/sortable'
], function($, _, Backbone) { return Backbone.View.extend({

  events: {
    "sortstart":  "sortstart",
    "sortupdate": "sortUpdate"
  },

  initialize() {
    this.listenTo(this.collection, 'add',    this.add);
    this.listenTo(this.collection, 'remove', this.remove);
    this.listenTo(this.collection, 'reset',  this.reset);
    
    return (this.$el.sortable)();
  },

  /*
  Create an instance of the subview as specified in this views attributes and
  append this subview to the supplied model for easy access later. 

  WARNING: This will mean that we can only add this model to one of these types
  of views
  */
  add(model) { 
    return model._subView = new this.attributes.subView({
      model,
      el: $('<li class="list-group-item"></li>').prependTo(this.$el)
    });
  },

  /*
  Remove the given models subview
  */
  remove(model) { return (model._subView.remove)(); },

  /*
  Scan over all the old models and remove the subviews then add the new ones
  */
  reset(models, options) {
    _.each(options.previousModels, model => this.remove(model));
    return models.forEach(model => this.add(model));
  },

  /*
  Event handler for when a sorting starts. We will store the position that
  the given element was in
  */
  sortStart(event, ui) {
    return this._oldPosition = this.collection.length - 1 - ui.item.index();
  },

  /*
  Event handler for when sorting has finished. Call the positioning method
  of the collection. NOTE we don't listen to updates in the position in this 
  view
  */
  sortUpdate(event, ui) {
    const newPosition = this.collection.length - 1 - ui.item.index();
    return this.collection.position(this._oldPosition, newPosition);
  }
});
 });