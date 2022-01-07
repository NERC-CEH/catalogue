/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!collections/Positionable',
  'cs!models/OnlineResourceLayer'
], function(Positionable, OnlineResourceLayer) { return Positionable.extend({
  model(attr, options) {
    if (attr.onlineResource) { return new OnlineResourceLayer(attr,options); }
  },

  initialize() {
    return this.on("change:opacity change:visibility reset", this.hideLayerInfo);
  },

  /*
  Scans over each of the layers contained within this collection and then hides
  the info display
  */
  hideLayerInfo() {
    return this.forEach(layer => layer.set('infoVisible', false));
  }
});
 });