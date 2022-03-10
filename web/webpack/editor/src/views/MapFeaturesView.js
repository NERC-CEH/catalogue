/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'cs!views/editor/ObjectInputView',
  'cs!views/editor/MapStyleSelectorView',
  'tpl!templates/editor/MapFeatures.tpl'
], function(Backbone, ObjectInputView, MapStyleSelectorView, template) { return ObjectInputView.extend({

  template,

  initialize(options) {    
    ObjectInputView.prototype.initialize.call(this, options);

    return new MapStyleSelectorView({
      el: this.$('.style-selector'),
      model: this.model.getRelated('style')
    });
  }
});
 });