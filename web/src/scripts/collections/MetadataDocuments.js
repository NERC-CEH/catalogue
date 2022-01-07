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
  'cs!models/MetadataDocument'
], function(_, Backbone, MetadataDocument) { return Backbone.Collection.extend({
  model: MetadataDocument,

  /*
  Takes the list of ids and sets these onto this model
  */
  setById(ids, options) { return this.set(_.map( ids, id => ({
    id
  })), options); }
});
 });