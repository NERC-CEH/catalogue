import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    value: ''
  },

  // Keyed by the option values in templates/InfrastructureChallenge
  uris: {
    'Climate change: mitigation': 'http://vocab.ceh.ac.uk/ri#Mitigation',
    'Climate change: adaptation': 'http://vocab.ceh.ac.uk/ri#Resilience',
    Pollution: 'http://vocab.ceh.ac.uk/ri#Pollution',
    'Sustainable ecosystems: biodiversity net gain': 'http://vocab.ceh.ac.uk/ri#Biodiversity'
  },

  initialize () {
    this.on('change:value', this.updateUri)
  },

  updateUri (model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : '')
  }
})
