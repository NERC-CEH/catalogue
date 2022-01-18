// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'cs!views/PermissionView'
], function(Backbone, PermissionView) { return Backbone.View.extend({
  el: '.permission',

  initialize() {
    return this.listenTo(this.model, 'loaded', this.render);
  },

  render() {
    const view = new PermissionView({
     model: this.model.getPermission()});
    (view.render)();
    return this;
  }
});
 });