import Backbone from 'backbone'
export default Backbone.Model.extend({

  defaults: {
    value: ''
  },

  uris: {
    'TA-RA project': 'https://elter-ri.eu/transnational-remote-access-ta-ra'
  },

  initialize () {
    this.on('change:value', this.updateUri)
  },

  updateUri (model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : '')
  }
})
