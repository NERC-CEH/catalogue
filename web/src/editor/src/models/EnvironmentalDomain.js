import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    value: ''
  },

  uris: {
    atmosphere: 'http://ex.org/domain/atmosphere',
    biosphere: 'http://ex.org/domain/biosphere',
    builtEnvironment: 'http://ex.org/domain/built environment',
    cryosphere: 'http://ex.org/domain/cryosphere',
    freshwater: 'http://ex.org/domain/freshwater',
    geosphere: 'http://ex.org/domain/geosphere',
    groundwater: 'http://ex.org/domain/groundwater',
    lithosphere: 'http://ex.org/domain/lithosphere',
    marine: 'http://ex.org/domain/marine',
    socioeconomic: 'http://ex.org/domain/socio-economic'

  },

  initialize () {
    this.on('change:value', this.updateUri)
  },

  updateUri (model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : '')
  }
})
