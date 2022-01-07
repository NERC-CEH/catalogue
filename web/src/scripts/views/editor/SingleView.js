// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'tpl!templates/editor/Single.tpl'
], function(Backbone, template) { return Backbone.View.extend({

  className: 'component',

  initialize(options) {
    this.data = options;
    if (!this.data.ModelType) {
      return this.data.ModelType = Backbone.Model;
    }
  },

  show() {
    return this.$el.addClass('visible');
  },

  hide() {
    return this.$el.removeClass('visible');
  },

  render() {
    this.$el.html(template({data: this.data}));
    return this;
  },

  updateMetadataModel(attribute) {
    return this.model.set(this.data.modelAttribute, attribute);
  }
});
 });
