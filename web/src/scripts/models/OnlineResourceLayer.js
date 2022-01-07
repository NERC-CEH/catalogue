/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'cs!models/Layer'
], function(_, Layer) { return Layer.extend({
  defaults: {
    opacity:     0.5,
    visibility:  false,
    infoVisible: false
  },

  initialize(attr, options) {
    this.onlineResource = attr.onlineResource;

    return Layer.prototype.initialize.call(this, arguments);
  }, //Initialize parent
    
  /*
  Returns the address to the tms end point which should be used when
  making wms requests
  */
  getTMS() { return `${this.onlineResource.url()}/tms/`; },

  /*
  Gets the address to the legend
  */
  getLegend() { return `${this.onlineResource.url()}/${this.getName()}/legend`; }
});
 });