/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'jquery',
  'cs!views/ExtentHighlightingMapView'
], function(_, $, ExtentHighlightingMapView) { return ExtentHighlightingMapView.extend({
  el: '#studyarea-map',

  initialize() {
    ExtentHighlightingMapView.prototype.initialize.call(this, arguments); //Initialize super
    return (this.render)();
  },

  /*
  Grab the locations off of the map element
  */
  getLocations() {
    const wkts = this.$('[dataType="geo:wktLiteral"]');
    return _.map(wkts, el => $(el).attr('content'));
  },

  /*
  Update the highlighted areas based upon the locations. Then zoom the to
  highlighted regions
  */
  render() {
    this.setHighlighted(this.getLocations());
    return (this.zoomToHighlighted)();
  }
});
 });
