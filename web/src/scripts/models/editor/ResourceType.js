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

  defaults: {
    value: ''
  },

  uris: {
    dataset: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/dataset',
    series: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/series',
    service: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/services'
  },

  initialize() {
    return this.on('change:value', this.updateUri);
  },

  updateUri(model, value) {
    return this.set('uri', this.uris[value] ? this.uris[value] : '');
  }
});
 });