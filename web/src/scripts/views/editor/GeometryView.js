// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS101: Remove unnecessary use of Array.from
 * DS102: Remove unnecessary code created because of implicit returns
 * DS205: Consider reworking code to avoid use of IIFEs
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'cs!views/editor/InputView',
  'tpl!templates/editor/Geometry.tpl',
  'cs!views/OpenLayersView',
  'openlayers'
],
function(_, InputView, template, OpenLayersView, OpenLayers) {

  const geometryLayer = new OpenLayers.Layer.Vector("Geometry");

  const controls = {
    create: new OpenLayers.Control.DrawFeature(
      geometryLayer,
      OpenLayers.Handler.Polygon
    ),
    modify: new OpenLayers.Control.ModifyFeature(geometryLayer),
    delete: new OpenLayers.Control.SelectFeature(geometryLayer,
      {onSelect(feature) { return geometryLayer.removeFeatures(feature); }}
    )
  };

  const view = InputView.extend({

    template,

    events: {
      'change input': 'toggleControls'
    },

    initialize(options) {
      InputView.prototype.initialize.call(this, options);
      this.stopListening();
      _.bindAll(this,
        'setModel'
      );

      geometryLayer.events.on({
        'beforefeatureadded'() { if (geometryLayer.features.length > 0) { return false; } },
        'afterfeaturemodified': this.setModel,
        'featureadded': this.setModel,
        'featureremoved': this.setModel
      });

      this.wktFactory = new OpenLayers.Format.WKT({
        internalProjection: this.map.baseLayer.projection,
        externalProjection: new OpenLayers.Projection('EPSG:4326')
      });

      if (this.model.has('geometry')) {
        return geometryLayer.addFeatures(this.wktFactory.read(this.model.get('geometry')));
      }
    },

    render() {
      InputView.prototype.render.apply(this);

      const mapView = new OpenLayersView({el: this.$('.map')});
      this.map = mapView.map;
      this.map.addLayer(geometryLayer);
      return this.map.addControls(_.values(controls));
    },

    toggleControls(event) {
      const {
        target
      } = event;
      return (() => {
        const result = [];
        for (let key of Array.from(_.keys(controls))) {
          const control = controls[key];
          if ((target.value === key) && target.checked) {
            result.push(control.activate());
          } else {
            result.push(control.deactivate());
          }
        }
        return result;
      })();
    },

    setModel() {
      const {
        features
      } = geometryLayer;
      if (features.length > 0) {
        if (features[0].geometry.getArea() === 0) { // prevent points being added
          return geometryLayer.removeFeatures(features[0]);
        } else {
          return this.model.set('geometry', this.wktFactory.write(features[0]));
        }
      } else {
        return this.model.unset('geometry');
      }
    }
  });

  return view;
});
