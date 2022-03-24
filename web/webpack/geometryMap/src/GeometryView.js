import _ from 'underscore'
import template from './Geometry.tpl'
import 'leaflet/dist/leaflet.css'
import './main.css'
import L from 'leaflet'
import { InputView } from '../../editor/src'

let map

export default InputView.View.extend({

  initialize (options) {
    InputView.prototype.initialize.call(this, options)
    this.render()
  },

  render () {
    this.$el.html(_.template(template))
  },

  createMap () {
    map = L.map('map').setView([51.505, -0.09], 13)
    L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={pk.eyJ1IjoidGhvc3RhY2VoIiwiYSI6ImNsMHY4b2s0cjAwaDEzYm15YXIzdGsxdGkifQ.zWEMWh3d4Z3zcb9AF1uxgA}', {
      attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
      maxZoom: 18,
      id: 'mapbox/streets-v11',
      tileSize: 512,
      zoomOffset: -1,
      accessToken: 'pk.eyJ1IjoidGhvc3RhY2VoIiwiYSI6ImNsMHY4b2s0cjAwaDEzYm15YXIzdGsxdGkifQ.zWEMWh3d4Z3zcb9AF1uxgA'
    }).addTo(map)
  },

  addSpacialExtent (northLng, southLng, westLat, eastLat) {
    L.rectangle([
      [westLat, northLng],
      [southLng, -eastLat]
    ]).addTo(map)
  },

  addMarker (lat, lng) {
    L.marker([lat, lng]).addTo(map)
  },

  addCircle (lat, lng) {
    L.circle([lat, lng], {
      color: 'red',
      fillColor: '#f03',
      fillOpacity: 0.5,
      radius: 500
    }).addTo(map)
  },

  addPolygon () {
    L.polygon([
      [51.509, -0.08],
      [51.503, -0.06],
      [51.51, -0.047]
    ]).addTo(map)
  }

})
