import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    value: ''
  },

  uris: {
    platform: 'http://ex.com/facilityType/platform',
    sensor: 'http://ex.com/facilityType/sensor',
    site: 'http://ex.com/facilityType/site',
    station: 'http://ex.com/facilityType/station'
  },

  initialize () {
    this.on('change:value', this.updateUri)
  },

  updateUri (model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : '')
  }
})
