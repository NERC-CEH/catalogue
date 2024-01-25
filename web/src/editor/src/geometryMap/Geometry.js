import Backbone from 'backbone'

export default Backbone.Model.extend({

  hasGeometry () {
    return this.has('geometryString')
  },

  getGeometry () {
    return this.get('geometryString')
  },

  setGeometry (geometry) {
    this.set('geometryString', geometry)
  },

  clearGeometry () {
    this.set('geometryString', null)
  }
})
