import Backbone from 'backbone'

export default Backbone.Model.extend({

  hasGeometry () {
    return this.has('geometry')
  },

  getGeometry () {
    return this.get('geometry')
  },

  setGeometry (geometry) {
    this.set('geometry', geometry)
  },

  clearGeometry () {
    this.set('geometry', null)
  }
})
