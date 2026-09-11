import L from 'leaflet'
import 'leaflet-draw'
import * as turf from '@turf/turf'
import { ObjectInputView } from '../views'
import template from './geometryTemplate'

export default ObjectInputView.extend({

  events: {
    'change #box': 'handleInput',
    'change #locationConfidential': 'handleLocationConfidentialCheckbox'
  },

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this, arguments)
    this.render()
    this.viewMap()
    this.listenTo(this.model, 'change:geometryString', function (model, value) {
      this.$('#box').val(value)
    })

    this.listenTo(this.model, 'change:locationConfidential', () => {
      this.handleLocationConfidentialChange()
    })
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

  handleLocationConfidentialCheckbox () {
    const isChecked = this.$('#locationConfidential').is(':checked')

    if (isChecked) {
      const geometryString = this.model.get('geometryString')

      if (geometryString) {
        try {
          const geoJson = JSON.parse(geometryString)

          const geometry = geoJson.type === 'Feature'
            ? geoJson.geometry
            : geoJson

          if (geometry?.type === 'Point') {
            const confirmed = window.confirm(
              'Marking a location as confidential will replace a point with an approximate location .\n\n' +
              'Do you want to continue?'
            )

            if (!confirmed) {
              this.$('#locationConfidential').prop('checked', false)
              return
            }
          }
        } catch (e) {
          console.error('Unable to parse geometry', e)
        }
      }
    }

    this.model.set('locationConfidential', isChecked)
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

    const isLocationConfidential = this.model?.get('locationConfidential') === true
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

  handleLocationConfidentialChange () {
    if (!this.map || !this.drawControl) {
      console.log('No map or drawControl, exiting')
      return
    }

    const isLocationConfidential = this.model?.get('locationConfidential') === true
    const rounding = (key, val) => {
      return typeof val === 'number' ? Number(val.toFixed(5)) : val
    }

    const hasGeometry = this.model.getGeometry?.()

    if (hasGeometry) {
      try {
        const currentGeometry = JSON.parse(this.model.get('geometryString'))
        const geometry = currentGeometry.type === 'Feature'
          ? currentGeometry.geometry
          : currentGeometry

        if (isLocationConfidential) {
          if (geometry.type === 'Point') {
            const bufferDistance = Number(2) // Distance (in km) for the buffer
            const [lng, lat] = geometry.coordinates

            // Round coordinates to 2 decimal places
            const roundedLng = Number(lng.toFixed(2))
            const roundedLat = Number(lat.toFixed(2))

            const center = turf.point([roundedLng, roundedLat])

            // Calculate points 500m N, S, E and W of the rounded centre
            const north = turf.destination(center, (bufferDistance / 2), 0)
            const south = turf.destination(center, (bufferDistance / 2), 180)
            const east = turf.destination(center, (bufferDistance / 2), 90)
            const west = turf.destination(center, (bufferDistance / 2), 270)

            const square = turf.polygon([[
              [west.geometry.coordinates[0], south.geometry.coordinates[1]], // SW
              [east.geometry.coordinates[0], south.geometry.coordinates[1]], // SE
              [east.geometry.coordinates[0], north.geometry.coordinates[1]], // NE
              [west.geometry.coordinates[0], north.geometry.coordinates[1]], // NW
              [west.geometry.coordinates[0], south.geometry.coordinates[1]] // Close polygon
            ]])

            square.properties.isConfidentialSquare = true

            this.model.setGeometry(JSON.stringify(square, rounding))

            this.drawnItems.clearLayers()
            const layer = L.geoJson(square)
            this.drawnItems.addLayer(layer)
            this.map.fitBounds(this.drawnItems.getBounds())
          } else {
            console.log('Geometry is not a Point, no conversion needed')
          }
        } else {
          if (
            geometry.type === 'Polygon' &&
            currentGeometry.properties?.isConfidentialSquare
          ) {
            const center = turf.centroid(currentGeometry)

            this.model.setGeometry(JSON.stringify(center, rounding))

            this.drawnItems.clearLayers()
            const layer = L.geoJson(center)
            this.drawnItems.addLayer(layer)
          } else {
            console.log('Geometry is not a confidential square, no conversion needed')
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
