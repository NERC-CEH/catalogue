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
  'cs!collections/Layers'
], function(_, Backbone, Layers) { return Backbone.Model.extend({

  /*
  The url for this OnlineResource is dynamic. It is only valid if this model
  has been attached to an OnlineResources collection
  */
  url() { return `/documents/${this.collection.metadataDocument.id}/onlineResources/${this.id}`; },

  /*
  Determing if this online resource represents an WMS online resource.
  Do this by checking if the type is Get Capabilities
  */
  isWms() { return this.get('type') === 'WMS_GET_CAPABILITIES'; },

  /*
  Generate a Layers collection for each of the layers which this OnlineResource
  can render
  */
  getLayers() { return _.map(this.attributes.layers, layer => { 
    return _.extend(layer, {onlineResource: this});
  }); }
});
 });