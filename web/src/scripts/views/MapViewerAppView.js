/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'backbone',
  'cs!views/MapViewerMapView',
  'cs!views/SortableCollectionView',
  'cs!views/LayerControlsView'
], function($, Backbone, MapViewerMapView, SortableCollectionView, LayerControlsView) { return Backbone.View.extend({
  el: '#mapviewer',

  initialize() {
    return (this.render)();
  },

  render() {
    this.mapViewerMapView = new MapViewerMapView({
      collection: this.model.getLayers(),
      el:         this.$('.openlayers')
    });

    return this.layersView = new SortableCollectionView({
      collection: this.model.getLayers(),
      el:         this.$('.layers .list-group'),
      attributes: {
        subView: LayerControlsView
      }
    });
  }
});
 });