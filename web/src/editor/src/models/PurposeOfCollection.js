import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    value: ''
  },

  uris: {
    contractual: 'http://ex.org/domain/contractual',
    dataCollection: 'http://ex.org/domain/dataCollection',
    dataSeries: 'http://ex.org/domain/dataSeries',
    cooperation: 'http://ex.org/domain/cooperation',
    emergencyResponse: 'http://ex.org/domain/emergencyResponse',
    innovation: 'http://ex.org/domain/innovation',
    international: 'http://ex.org/domain/international',
    legislative: 'http://ex.org/domain/legislative',
    competence: 'http://ex.org/domain/competence',
    ministerialCommitment: 'http://ex.org/domain/ministerialCommitment',
    modelling: 'http://ex.org/domain/modelling',
    moral: 'http://ex.org/domain/moral',
    policy: 'http://ex.org/domain/policy',
    statuoryAdvice: 'http://ex.org/domain/statuoryAdvice',
    strategic: 'http://ex.org/domain/strategic'
  },

  initialize () {
    this.on('change:value', this.updateUri)
  },

  updateUri (model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : '')
  }
})
