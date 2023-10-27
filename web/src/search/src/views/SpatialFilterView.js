import _ from 'underscore'
import L from 'leaflet'
import 'leaflet-draw'
import template from '../templates/spatialFitlerTemplate'
import Backbone from "backbone";

export default Backbone.View.extend({

    initialize () {
        this.template = template
        this.render()
    },

    createMap () {
        const baseMaps = {
            Map: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 18,
                attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
            }),
            Satellite: L.tileLayer('http://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
                attribution: 'google'
            })
        }

        this.map = new L.Map(this.$('.map')[0], { center: new L.LatLng(54, 1), zoom: 4 })
        this.map.addLayer(baseMaps.Map)
        this.drawnItems = L.featureGroup()
        this.drawControl = this.createToolbar()
        this.drawnItems.addTo(this.map)
        L.control.layers(baseMaps, { Drawlayer: this.drawnItems }, { position: 'topright', collapsed: false }).addTo(this.map)
        this.map.addControl(this.drawControl)
        baseMaps.Map.addTo(this.map)

        this.listenTo(this.map, L.Draw.Event.CREATED, function (event) {
            this.boundingBox = event.layer
            this.drawnItems.addLayer(this.boundingBox)
            this.shapeDrawn = true
            this.map.removeControl(this.drawControl)
            this.drawControl = this.deleteToolbar()
            this.map.addControl(this.drawControl)
            this.setBbox(this.boundingBox)
        })

        this.listenTo(this.map, L.Draw.Event.DELETED, function () {
            this.map.removeControl(this.drawControl)
            this.drawControl = this.createToolbar()
            this.map.addControl(this.drawControl)
            this.model.clearBbox()
        })
    },

    createToolbar () {
        return new L.Control.Draw({
            position: 'topleft',
            edit: {
                featureGroup: this.drawnItems,
                edit: false,
                remove: false
            },
            draw: {
                rectangle: true,
                polygon: false,
                polyline: false,
                marker: false,
                circle: false,
                circlemarker: false
            }
        })
    },

    deleteToolbar () {
        return new L.Control.Draw({
            position: 'topleft',
            edit: {
                featureGroup: this.drawnItems,
                edit: false,
                remove: true
            },
            draw: {
                rectangle: false,
                polygon: false,
                polyline: false,
                marker: false,
                circle: false,
                circlemarker: false
            }
        })
    },

    setBbox(bounds){
        // Not sure if this is what the search requires, but bbox is WGS84: left,bottom,right,top
        this.model.setBbox([bounds._bounds._northEast.lng,bounds._bounds._northEast.lat,bounds._bounds._southWest.lng,bounds._bounds._southWest.lat])
    },

    viewMap () {
        if (this.map) {
            this.map.off()
            this.map.remove()
        }
        this.createMap()
    },

    render () {
        if (this.shapeDrawn === true) {
            this.viewMap()
        }
        this.viewMap()
        return this
    }
})
