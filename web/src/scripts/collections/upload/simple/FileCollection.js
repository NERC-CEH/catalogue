// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'cs!models/upload/simple/File'
], function(Backbone, File) { return Backbone.Collection.extend({
    model: File,

    comparator: 'name',

    initialize(options) {
      return this.url = options.url;
    }
});
 });
