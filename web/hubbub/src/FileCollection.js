/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'cs!models/upload/hubbub/File'
], (Backbone, File) => Backbone.Collection.extend({
    model: File}));
