// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone'
], Backbone => Backbone.Model.extend({
  idAttribute: "identity",

  defaults: {
    canView: false,
    canEdit: false,
    canDelete: false,
    canUpload: false
  }
}));