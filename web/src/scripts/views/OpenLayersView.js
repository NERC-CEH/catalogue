/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'underscore',
  'backbone',
  'openlayers'
], function($, _, Backbone, OpenLayers) { return Backbone.View.extend({
  initialize() {
    this.map = new OpenLayers.Map({
      div: this.el,
      maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34),
      displayProjection: new OpenLayers.Projection("EPSG:3857"),
      theme: null
    });

    this.map.addControl(new OpenLayers.Control.LoadingPanel);

    const backdrop = new OpenLayers.Layer.OSM("OSM", [
      "//a.tile.openstreetmap.org/${z}/${x}/${y}.png",
      "//b.tile.openstreetmap.org/${z}/${x}/${y}.png",
      "//c.tile.openstreetmap.org/${z}/${x}/${y}.png"
    ]);

    this.map.addLayers([backdrop]);
    return (this.refresh)();
  },

  /*
  Reset the position and size of the map. Parent views should call this method 
  when the map reappears onscreen
  */
  refresh() {
    (this.map.updateSize)();
    return this.map.zoomToExtent(new OpenLayers.Bounds(-1885854.36, 6623727.12, 1245006.31, 7966572.83));
  }
});
 });