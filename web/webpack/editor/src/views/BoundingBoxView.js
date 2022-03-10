import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import template from '../templates/BoundingBox.tpl'
import OpenLayersView from './OpenLayersView'
import OpenLayers from '../../../../src/vendor/OpenLayers-custom'

export default ObjectInputView.extend({

    events () {
      return _.extend({}, ObjectInputView.prototype.events,
        { 'click button': 'viewMap' })
    },

    initialize (options) {
      this.template = _.template(template)
      _.bindAll(this,
        'handleDrawnFeature',
        'handleTransformedFeature'
      );
      (this.render)()
      this.listenTo(this.model, 'change:westBoundLongitude', function (model, value) {
        return this.$('#boundingBoxWestBoundLongitude').val(value)
      })
      this.listenTo(this.model, 'change:southBoundLatitude', function (model, value) {
        return this.$('#boundingBoxSouthBoundLatitude').val(value)
      })
      this.listenTo(this.model, 'change:eastBoundLongitude', function (model, value) {
        return this.$('#boundingBoxEastBoundLongitude').val(value)
      })
      this.listenTo(this.model, 'change:northBoundLatitude', function (model, value) {
        return this.$('#boundingBoxNorthBoundLatitude').val(value)
      })
      return this.listenTo(this.model.collection, 'visible', this.viewMap)
    },

    viewMap () {
      const $map = this.$('.map')
      $map.html('')
      const mapView = new OpenLayersView({ el: $map })
      this.map = mapView.map

      this.boundingBoxLayer = new OpenLayers.Layer.Vector('Bounding Box')
      this.map.addLayer(this.boundingBoxLayer)

      this.transform = new OpenLayers.Control.TransformFeature(this.boundingBoxLayer, {
        rotate: false,
        irregular: true
      }
      )
      this.transform.events.register(
        'transformcomplete', this.boundingBoxLayer, this.handleTransformedFeature
      )
      this.map.addControl(this.transform)

      this.drawing = new OpenLayers.Control.DrawFeature(this.boundingBoxLayer,
        OpenLayers.Handler.RegularPolygon, {
          title: 'Draw Bounding Box',
          handlerOptions: {
            sides: 4,
            irregular: true
          }
        }
      )
      this.drawing.events.register(
        'featureadded', this.boundingBoxLayer, this.handleDrawnFeature
      )
      this.map.addControl(this.drawing)

      if (this.model.hasBoundingBox()) {
        (this.createFeature)()
        this.map.zoomToExtent(this.boundingBoxLayer.getDataExtent())
        return (this.transform.activate)()
      } else {
        (mapView.refresh)()
        return (this.drawing.activate)()
      }
    },

    createFeature () {
      const boundingBox = this.model.getBoundingBox()
      this.boundingBoxLayer.addFeatures([boundingBox])
      return this.transform.setFeature(boundingBox)
    },

    handleDrawnFeature (obj) {
      (this.drawing.deactivate)();
      (this.transform.activate)()
      this.transform.setFeature(obj.feature)
      return this.handleTransformedFeature(obj)
    },

    handleTransformedFeature (obj) {
      const bounds = obj.feature.geometry
        .clone()
        .transform('EPSG:3857', 'EPSG:4326')
        .getBounds()

      return this.model.set({
        westBoundLongitude: bounds.left.toFixed(3),
        southBoundLatitude: bounds.bottom.toFixed(3),
        eastBoundLongitude: bounds.right.toFixed(3),
        northBoundLatitude: bounds.top.toFixed(3)
      })
    }
})
