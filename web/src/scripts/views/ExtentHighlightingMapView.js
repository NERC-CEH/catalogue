// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * DS207: Consider shorter variations of null checks
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'underscore',
  'backbone',
  'cs!views/OpenLayersView',
  'openlayers'
], function($, _, Backbone, OpenLayersView, OpenLayers) { return OpenLayersView.extend({
  highlighted: {
    strokeColor: '#8fca89',
    fillColor:   '#8fca89',
    fillOpacity: 0.3
  },

  minGeoLength: 20, // The minimum size in pixels a feature will be displayed as
  wktFactory:   new OpenLayers.Format.WKT,
  epsg4326:     new OpenLayers.Projection("EPSG:4326"),

  initialize() {
    OpenLayersView.prototype.initialize.call(this, arguments); //Initialize super

    // Create a vector layer which will render the selected features extents. If
    // they are too small to see on screen, points will be symbolized in there 
    // place
    this.highlighted.pointRadius = this.minGeoLength / 2;
    this.highlightedLayer = new OpenLayers.Layer.Vector("Selected Layer", { 
      styleMap: new OpenLayers.StyleMap(new OpenLayers.Style(this.highlighted,
        {rules: [ new OpenLayers.Rule({filter: this.createFilterFunction()}) ]}))
    });

    return this.map.addLayer(this.highlightedLayer);
  },


  /*
  Define an openlayers filter function which will dictate if a geometry should
  be rendered or not. The function is designed to work over features generated
  by the "setHighlighted" method
  */
  createFilterFunction() { return new OpenLayers.Filter.Function({
    evaluate: geo => geo.isPoint === !this.isLengthVisible(geo.areaRoot)}); },

  /*
  Decide if it is possible (or at least sensible) to render a feature with the 
  given length on the map. If the length is too small, favour a fixed size
  point instead.
  */
  isLengthVisible(length) { return (length / this.map.getResolution()) > this.minGeoLength; },

  /*
  Position the openlayers map such that the features of the highlighted layer 
  can be seen
  */
  zoomToHighlighted() { return this.map.zoomToExtent(this.highlightedLayer.getDataExtent()); },

  /*
  Given an array of locations in the form:

    ["POLYGON((1 2 ...))", "POLYGON((1 2 ...))"]

  Draw these on the map. If this method is called with null or an empty array
  then remove all the highlighted features from the map
  */
  setHighlighted(locations) {
    // Remove all the old markers
    if (locations == null) { locations = []; }
    (this.highlightedLayer.removeAllFeatures)();

    // Loop round all the locations and set as a marker and polygon
    return _.each(locations, location => {
      if (location === "") { 
        return;
      }
      const vector = this.readWKT(location);

      // Calculate the average length of the height and width of the bounds
      const bounds = vector.geometry.getBounds().toGeometry();
      vector.attributes = { 
        areaRoot: Math.sqrt(bounds.getArea()),
        isPoint:  false
      };

      const point = new OpenLayers.Feature.Vector(bounds.getCentroid());
      point.attributes = _.defaults({isPoint: true}, vector.attributes);
      return this.highlightedLayer.addFeatures([vector, point]);
  });
  },

  /*
  Transform the well known text representation into an openlayers geometry in
  the projection system of the map
  */
  readWKT(wkt) {
    const vector = this.wktFactory.read(wkt);
    vector.geometry.transform(this.epsg4326, this.map.getProjectionObject());
    return vector;
  },

  /*
  Convert the given location string into a well known text representation.
  Solr geometries can either be Points or Polygons
  */
  solr2WKT(location) {
    const c = location.split(' ');
    if (c.length === 2) { return `POINT(${c[0]} ${c[1]})`; 
    } else { return `POLYGON((${c[0]} ${c[1]}, \
${c[0]} ${c[3]}, \
${c[2]} ${c[3]}, \
${c[2]} ${c[1]}, \
${c[0]} ${c[1]}))`; }
  }
});
 });

