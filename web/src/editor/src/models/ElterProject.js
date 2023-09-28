import Backbone from 'backbone'
export default Backbone.Model.extend({

  defaults: {
    value: ''
  },

  uris: {
    'TA-RA project': 'http://vocabs.lter-europe.net/elter_cl/10684',
    'e-shape': ''
  },

  initialize () {
    this.on('change:value', this.updateUri)
  },

  updateUri (model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : '')
  }
})
