import L from 'leaflet'
import 'leaflet-draw'
import * as turf from '@turf/turf'
import { ObjectInputView } from '../views'
import template from './geometryTemplate'

export default ObjectInputView.extend({

  events: {
    'change #box': 'handleInput'
  },

  initialize (options) {
    this.template = template
    this.parentModel = options?.parentModel || this.options?.parentModel
    ObjectInputView.prototype.initialize.apply(this, arguments)
    this.render()
    this.viewMap()
    this.listenTo(this.model, 'change:geometryString', function (model, value) {
      this.$('#box').val(value)
    })

    if (this.parentModel) {
      this.listenTo(this.parentModel, 'change:locationConfidential', () => {
        this.handleLocationConfidentialChange()
      })
    } else {
      console.warn('No parentModel found - locationConfidential changes will not trigger geometry conversion')
    }
  },

  getGeometry () {
    const parsedJson = JSON.parse(this.model.get('geometryString'))
    return L.geoJson(parsedJson)
  },

  handleInput () {
    this.model.setGeometry(this.$('#box').val())

    // Redraw shape
    this.drawnItems.clearLayers()
    this.drawnItems.addLayer(this.getGeometry())
  },

  createMap () {
    this.map = new L.Map(this.$('.map')[0], { center: new L.LatLng(51.513, -0.09), zoom: 4 })

    this.drawnItems = L.featureGroup()
    if (this.model.getGeometry()) {
      this.geometryString = this.getGeometry()
      this.drawButtons = false
      this.drawnItems.addLayer(this.geometryString)

      // Zoom to polygon if one was provided
      if (this.model.hasGeometry()) {
        if (this.model.getGeometry().toLowerCase().includes('polygon')) {
          this.map.fitBounds(this.drawnItems.getBounds())
        }
      }
    } else {
      this.drawButtons = true
    }
    this.drawControl = this.createToolbar()
    this.drawnItems.addTo(this.map)

    const baseMaps = {
      Map: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }),
      Satellite: L.tileLayer('http://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      })
    }

    L.control.layers(baseMaps, { drawlayer: this.drawnItems }, {
      position: 'topright',
      collapsed: false
    }).addTo(this.map)

    this.map.addControl(this.drawControl)
    baseMaps.Map.addTo(this.map)

    const rounding = function (key, val) {
      if (typeof val === 'number') { return Number(val.toFixed(5)) }
      return val
    }

    this.currentTool = null
    this.circleDrawingEnabled = false

    this.circleClickHandler = (e) => {
      if (!this.circleDrawingEnabled) return

      const point = turf.point([e.latlng.lng, e.latlng.lat])
      const buffered = turf.buffer(point, 2, { units: 'kilometers' })
      buffered.properties.isTurfCircle = true

      this.drawnItems.clearLayers()
      const layer = L.geoJson(buffered)
      this.drawnItems.addLayer(layer)
      this.model.setGeometry(JSON.stringify(buffered, rounding))

      this.circleDrawingEnabled = false
      this.currentTool = null
      this.drawButtons = false

      this.map.removeControl(this.drawControl)
      this.drawControl = this.createToolbar()
      this.map.addControl(this.drawControl)
      this.map._container.style.cursor = ''
    }

    this.map.on('click', this.circleClickHandler)

    this.listenTo(this.map, L.Draw.Event.CREATED, function (event) {
      const layer = event.layer
      const geoJson = JSON.stringify(layer.toGeoJSON(), rounding)
      this.model.setGeometry(geoJson)
      this.drawButtons = false
      this.map.removeControl(this.drawControl)
      this.drawControl = this.createToolbar()
      this.map.addControl(this.drawControl)
      this.drawnItems.addLayer(layer)
    })

    this.listenTo(this.map, L.Draw.Event.DELETED, function () {
      this.model.clearGeometry()
      this.drawButtons = true
      this.map.removeControl(this.drawControl)
      this.drawControl = this.createToolbar()
      this.map.addControl(this.drawControl)
    })
  },

  createToolbar () {
    this.deleteButton = this.drawButtons !== true

    const isLocationConfidential = this.parentModel?.get('locationConfidential') || false
    const toolbar = new L.Control.Draw({
      position: 'topleft',
      edit: {
        featureGroup: this.drawnItems,
        edit: false,
        remove: this.deleteButton
      },
      draw: {
        rectangle: false,
        polygon: this.drawButtons,
        polyline: false,
        marker: this.drawButtons && !isLocationConfidential,
        circle: false,
        circlemarker: false
      }
    })

    const originalOnAdd = toolbar.onAdd.bind(toolbar)
    toolbar.onAdd = (map) => {
      const container = originalOnAdd(map)
      if (this.drawButtons && isLocationConfidential) {
        this.addCircleButton(container)
      }
      return container
    }

    return toolbar
  },

  isCircleGeometry (geojson) {
    const feature = geojson.type === 'Feature' ? geojson : { type: 'Feature', geometry: geojson, properties: {} }
    if (feature.properties && feature.properties.isTurfCircle) {
      return true
    }
  },

  handleLocationConfidentialChange () {
    if (!this.map || !this.drawControl) {
      console.log('No map or drawControl, exiting')
      return
    }

    const isLocationConfidential = this.parentModel?.get('locationConfidential') || false
    const rounding = (key, val) => {
      return typeof val === 'number' ? Number(val.toFixed(5)) : val
    }

    const hasGeometry = this.model.getGeometry?.()
    if (hasGeometry) {
      try {
        const currentGeometry = JSON.parse(this.model.get('geometryString'))
        const geometry = currentGeometry.type === 'Feature' ? currentGeometry.geometry : currentGeometry

        if (isLocationConfidential) {
          if (geometry.type === 'Point') {
            const coords = geometry.coordinates
            const point = turf.point(coords)
            const buffered = turf.buffer(point, 2, { units: 'kilometers' })
            buffered.properties.isTurfCircle = true

            this.model.setGeometry(JSON.stringify(buffered, rounding))
            this.drawnItems.clearLayers()
            const layer = L.geoJson(buffered)
            this.drawnItems.addLayer(layer)
            this.map.fitBounds(this.drawnItems.getBounds())
          } else {
            console.log('Geometry is not a Marker, no conversion needed')
          }
        } else {
          if (geometry.type === 'Polygon' && this.isCircleGeometry(currentGeometry)) {
            const center = turf.centroid(currentGeometry)
            this.model.setGeometry(JSON.stringify(center, rounding))
            this.drawnItems.clearLayers()
            const layer = L.geoJson(center)
            this.drawnItems.addLayer(layer)
          } else {
            console.log('Geometry is not a circle, no conversion needed')
          }
        }
      } catch (e) {
        console.error('Error converting geometry:', e)
      }
    } else {
      console.log('No existing geometry to convert')
    }

    this.map.removeControl(this.drawControl)
    this.drawControl = this.createToolbar()
    this.map.addControl(this.drawControl)
  },

  addCircleButton (container) {
    const drawToolbar = container.querySelector('.leaflet-draw-draw-polygon')?.parentElement

    if (!drawToolbar) return

    const circleButton = L.DomUtil.create('a', 'leaflet-draw-draw-circle', drawToolbar)
    circleButton.href = '#'
    circleButton.title = 'Draw a 2km circle'

    L.DomEvent.on(circleButton, 'click', (e) => {
      L.DomEvent.preventDefault(e)
      L.DomEvent.stopPropagation(e)

      if (this.circleDrawingEnabled) {
        this.circleDrawingEnabled = false
        this.currentTool = null
        circleButton.classList.remove('leaflet-draw-toolbar-button-enabled')
        this.map._container.style.cursor = ''
      } else {
        this.circleDrawingEnabled = true
        this.currentTool = 'turf-circle'
        circleButton.classList.add('leaflet-draw-toolbar-button-enabled')
        this.map._container.style.cursor = 'crosshair'

        const polygonButton = drawToolbar.querySelector('.leaflet-draw-draw-polygon')
        if (polygonButton) {
          polygonButton.classList.remove('leaflet-draw-toolbar-button-enabled')
        }
      }
    })
  },

  viewMap () {
    if (this.map) {
      this.map.off()
      this.map.remove()
    }
    this.createMap()
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    return this
  }
})
