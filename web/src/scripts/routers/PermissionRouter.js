// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone'
], function(Backbone) { return Backbone.Router.extend({

  routes: {
    'permission/:identifier': 'loadPermission'
  },

  initialize(options) {
    return this.model = options.model;
  },

  loadPermission(identifier) {
    return this.model.loadPermission(identifier);
  }
});
 });