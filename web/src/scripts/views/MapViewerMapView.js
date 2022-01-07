// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'underscore',
  'backbone',
  'cs!views/OpenLayersView',
  'openlayers'
], function($, _, Backbone, OpenLayersView, OpenLayers) { return OpenLayersView.extend({
  initialize() {
    OpenLayersView.prototype.initialize.call(this, arguments); //Initialize super
    
    this.listenTo(this.collection, "add", this.addLayer);
    this.listenTo(this.collection, "position", this.positionLayer);
    this.listenTo(this.collection, "reset", this.resetLayers);
    return this.listenTo(this.collection, "remove", this.removeLayer);
  },

  /*
  Add the given layer to the map by creating a new OpenLayers WMS layer and 
  then appending this to the layer object. Position this layer such that it
  appears above the baselayer and any other layers
  */
  addLayer(layer) {
    this.map.addLayer(layer._openlayersTMS = this._createLayer(layer));
    return this.map.setLayerIndex(layer._openlayersTMS, this.map.getNumLayers() - 1);
  },
    
  /*
  Listens to when layers have been repositioned. Notify the OpenLayers Map and set the 
  new index for that layer
  */
  positionLayer(layer, collection, newPosition){
    return this.map.setLayerIndex(layer._openlayersTMS, newPosition + 1);
  },

  /*
  Remove the wms layer associated with the given layer
  */
  removeLayer(layer){ return this.map.removeLayer(layer._openlayersTMS); },

  /*
  Remove all the old wms layers and replace with the reset collection
  */
  resetLayers(layers, options) {
    _.each(options.previousModels, layer => this.removeLayer(layer));
    return layers.forEach(layer => this.addLayer(layer));
  },

  /*
  Create an openlayers layer given some model/Layer which updates when different parts
  of the layer change
  */
  _createLayer(layer) { 
    const tmsLayer = new OpenLayers.Layer.TMS(layer.getName(), layer.getTMS(), {
        layername:   layer.getName(),
        type:        'png',
        isBaseLayer: false,
        opacity:     layer.getOpacity(),
        visibility:  layer.isVisible()
      }
    );

    layer.on('change:opacity', () => tmsLayer.setOpacity(layer.getOpacity()));
    layer.on('change:visibility', () => tmsLayer.setVisibility(layer.isVisible()));
    return tmsLayer;
  }
});
 });