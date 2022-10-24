import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    value: ''
  },

  uris: {
    Biodiversity: 'http://vocab.ceh.ac.uk/ri#Biodiversity',
    Pollution: 'http://vocab.ceh.ac.uk/ri#Pollution',
    'Climate change adaptation': 'http://vocab.ceh.ac.uk/ri#Resilience',
    'Climate change mitigation': 'http://vocab.ceh.ac.uk/ri#Mitigation'
  },

  initialize () {
    this.on('change:value', this.updateUri)
  },

  updateUri (model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : '')
  }
})
