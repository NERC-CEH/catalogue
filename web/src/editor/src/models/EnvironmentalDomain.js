import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    value: ''
  },

  uris: {
    Atmosphere: 'http://ex.org/domain/atmosphere',
    Biosphere: 'http://ex.org/domain/biosphere',
    'Built environment': 'http://ex.org/domain/builtEnvironment',
    Cryosphere: 'http://ex.org/domain/cryosphere',
    Freshwater: 'http://ex.org/domain/freshwater',
    Geosphere: 'http://ex.org/domain/geosphere',
    Groundwater: 'http://ex.org/domain/groundwater',
    Lithosphere: 'http://ex.org/domain/lithosphere',
    Marine: 'http://ex.org/domain/marine',
    'Socio-economic': 'http://ex.org/domain/socio-economic'

  },

  initialize () {
    this.on('change:value', this.updateUri)
  },

  updateUri (model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : '')
  }
})
