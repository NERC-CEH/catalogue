import Backbone from 'backbone'

export default Backbone.Model.extend({

  hasPolygon () {
    return this.has('polygon')
  },

  getPolygon () {
    return this.get('polygon')
  },

  setPolygon (bounds) {
    this.set('polygon')
  },

  clearPolygon () {
    this.set('polygon', null)
  },

  hasMarker () {
    return this.has('marker')
  },

  getMarker () {
    return this.get('marker')
  },

  setMarker (bounds) {
    this.set('marker')
  },

  clearMarker () {
    this.set('marker', null)
  }
})
