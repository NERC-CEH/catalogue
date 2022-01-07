// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'backbone',
  'cs!models/Permission'
], function(_, Backbone, Permission) { return Backbone.Model.extend({

  loadPermission(identifier) {
    const permission = new Permission({
      id: identifier});

    return permission.fetch({
      success: model => {
        (model.loadCollection)();
        this.set('permission', model);
        return this.trigger('loaded');
      },

      error: model => {
        return this.trigger('error', `Unable to load permission for: ${model.id}`);
      }
    });
  },

  getPermission() {
    return _.clone(this.get('permission'));
  }
});
 });