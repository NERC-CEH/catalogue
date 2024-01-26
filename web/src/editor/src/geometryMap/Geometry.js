import Backbone from 'backbone'

export default Backbone.Model.extend({

  hasGeometry () {
    return this.has('geometryString')
  },

  getGeometry () {
    return this.get('geometryString')
  },

  setGeometry (geometryString) {
    this.set('geometryString', geometryString)
  },

  clearGeometry () {
    this.set('geometryString', null)
  }
})
