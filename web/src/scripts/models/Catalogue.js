// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone'
], function(Backbone) { return Backbone.Model.extend({

  url() {
    return (this.urlRoot)();
  },

  urlRoot() {
    return `/documents/${this.id}/catalogue`;
  }
});
 });
