// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'cs!models/OnlineResource'
], function(Backbone, OnlineResource) { return Backbone.Collection.extend({
  model: OnlineResource,

  /*
  Initialize a collection of Online Resources. Obviously online resources are 
  tied to a particular MetadataDocument. Therefore we require that metadata 
  document to be supplied in the options.
  */
  initialize(models, options) {
    return this.metadataDocument = options.metadataDocument;
  },

  /*
  Filter the collection to only online resources which are wms resources
  */
  getWmsResources() { return this.filter(onlineResource => onlineResource.isWms()); }
});
 });