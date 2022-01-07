// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS101: Remove unnecessary use of Array.from
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'backbone',
  'cs!collections/MetadataDocuments',
  'cs!collections/Layers'
], function(_, Backbone, MetadataDocuments, Layers) { return Backbone.Model.extend({

  initialize() {
    this.metadataDocuments = new MetadataDocuments();
    this.layers = new Layers;

    this.on('change:metadataIds', this.fetchMetadataDocuments);

    _.bindAll(this, 'handledSyncedResource', 'handleError');
    
    this.metadataDocuments.on('error',           this.handleError);
    this.metadataDocuments.on('resources-error', this.handleError);
    return this.metadataDocuments.on('resources-sync',  this.handledSyncedResource);
  },


  /*
  Listens to when an online resource has been synced. This is the case when
  that online resource represents a wms get capabilities. Since we now have it
  get the list of layers which it specifies to our layers collection
  */
  handledSyncedResource(model) { return this.layers.add(model.getLayers()); },

  /*
  The list of metadata document ids has changes. Clear out the metadata 
  documents collection and populate with the new set
  */
  fetchMetadataDocuments() {
    (this.layers.reset)(); // Clear out any set layers which we may already have.
    this.metadataDocuments.setById(this.get('metadataIds'));
    return this.metadataDocuments.forEach(doc => (doc.fetch)());
  },

  /*
  Grab layers collection from this model
  */
  getLayers() { return this.layers; },

  /*
  Handle any sync errors from either a resource or metadata document. Flag an
  error event so that we can display this in the message panel.
  */
  handleError(...args) { return this.trigger('error', ...Array.from(args)); }
});
 });
