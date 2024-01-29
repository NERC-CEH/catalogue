import Backbone from 'backbone'
export default Backbone.Model.extend({

  defaults: {
    value: ''
  },

  uris: {
    'Bio-DT': 'https://doi.org/10.3030/101057437',
    'e-shape': '',
    'TA-RA project': 'http://vocabs.lter-europe.net/elter_cl/10684'
  },

  initialize () {
    this.on('change:value', this.updateUri)
  },

  updateUri (model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : '')
  }
})
