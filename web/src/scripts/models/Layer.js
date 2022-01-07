/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'backbone'
], function(_, Backbone) { return Backbone.Model.extend({

  initialize() {
    return this.on('change:opacity', function() { return this.set('visibility', true); });
  },

  /*
  Get the current opacity value between 0-1 for this layer
  */
  getOpacity() { return this.get("opacity"); },

  /*
  Check if this layer is visible and should be renedered
  */
  isVisible() { return this.get("visibility"); },

  /*
  Returns the name of the layer which should be used to reference this layer
  when called agaist its tms end point
  */
  getName() { return this.get("name"); },

  /*
  Returns the name of the layer as should be displayed in the legend
  */
  getTitle() { return this.get("title"); },

  /*
  Returns the address to the tms end point which should be used when
  making tms requests
  */
  getTMS() { return this.get("tms"); },

  /*
  Returns the address to an image which represents the legend of this layer
  */
  getLegend() { return this.get("legend"); },

  /*
  Sets the info visibility for this layer to the value specified. If we are 
  going to show then hide any other layers info in the collection
  */
  setInfoVisibility(visible){
    if (visible) { (this.collection.hideLayerInfo)(); }
    return this.set('infoVisible', visible);
  }
});
 });
