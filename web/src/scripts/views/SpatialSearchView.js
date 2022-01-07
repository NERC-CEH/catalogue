// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * DS103: Rewrite code to no longer use __guard__, or convert again using --optional-chaining
 * DS207: Consider shorter variations of null checks
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'underscore',
  'cs!views/ExtentHighlightingMapView',
  'openlayers'
], function($, _, ExtentHighlightingMapView, OpenLayers) { return ExtentHighlightingMapView.extend({
  restriction: {
    strokeColor: '#1c1b1e',
    fillOpacity: 0
  },

  initialize() {
    ExtentHighlightingMapView.prototype.initialize.call(this, arguments); //Initialize super

    // Create the layers to draw the search results on
    this.drawingLayer = new OpenLayers.Layer.Vector("Drawing Layer", {style: this.restriction});

    this.drawingControl = new OpenLayers.Control.DrawFeature(this.drawingLayer, 
      OpenLayers.Handler.RegularPolygon, { 
      handlerOptions: {
        sides: 4,
        irregular: true
      }
    }
    );

    // Bind the handle drawn feature method to this class before registering it
    // as an openlayers event listener
    _.bindAll(this, 'handleDrawnFeature');
    this.drawingLayer.events.register("featureadded", this.drawingLayer, this.handleDrawnFeature);

    this.map.addLayer(this.drawingLayer);
    this.map.addControl(this.drawingControl);

    (this.updateHighlightedRecord)();
    this.listenTo(this.model, 'cleared:results results-change:selected', this.updateHighlightedRecord);
    this.listenTo(this.model, 'change:drawing', this.updateDrawingMode);
    return this.listenTo(this.model, 'change:bbox', this.updateDrawingLayer);
  },

  /*
  Update the drawing layer with the restricted bounding box used for searching.
  */
  updateDrawingLayer() {
    (this.drawingLayer.removeAllFeatures)(); // Remove all the drawn features
    if (this.model.has('bbox')) {               // Draw the bbox if specified
      return this.drawingLayer.addFeatures(this.readWKT(this.model.get('bbox')));
    }
  },


  /*
  Toggle the drawing control depending on weather or not the model is in 
  drawing mode
  */
  updateDrawingMode() {
    const mode = this.model.get('drawing') ? 'activate' : 'deactivate';
    return (this.drawingControl[mode])();
  },

  /*
  Obtain the drawn bounding box from the drawing layer and register it as a new
  bounding box to search within on the model
  */
  handleDrawnFeature(evt) {
    const feature = evt.feature.clone(); //clone the feature so we can perform a transformation
    feature.geometry.transform(this.map.getProjectionObject(), this.epsg4326); //convert to 4326
    return this.model.set('bbox', feature.geometry.toString());
  },

  /*
  Set the highlighted records based upon the current search result's locations
  */
  updateHighlightedRecord() {
    if (__guard__(__guard__(this.model.getResults(), x1 => x1.getSelectedResult()), x => x.locations) != null) { return this.setHighlighted(__guard__(__guard__(this.model.getResults(), x3 => x3.getSelectedResult()), x2 => x2.locations)); }
  }
});
 });

function __guard__(value, transform) {
  return (typeof value !== 'undefined' && value !== null) ? transform(value) : undefined;
}