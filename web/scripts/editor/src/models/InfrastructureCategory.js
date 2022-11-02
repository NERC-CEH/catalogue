import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    value: null
  },

  uris: {
    instrumentedSites: 'http://vocabs.ceh.ac.uk/ri/instrumentedSites',
    surveys: 'http://vocabs.ceh.ac.uk/ri/surveys',
    wildlifeSchemes: 'http://vocabs.ceh.ac.uk/ri/wildlifeSchemes',
    discoveryCollections: 'http://vocabs.ceh.ac.uk/ri/discoveryCollections',
    mobilePlatforms: 'http://vocabs.ceh.ac.uk/ri/mobilePlatforms',
    controlledPlatforms: 'http://vocabs.ceh.ac.uk/ri/controlledPlatforms',
    fieldPlatforms: 'http://vocabs.ceh.ac.uk/ri/fieldPlatforms',
    labsAnalysis: 'http://vocabs.ceh.ac.uk/ri/labsAnalysis',
    labsTest: 'http://vocabs.ceh.ac.uk/ri/labsTest',
    data: 'http://vocabs.ceh.ac.uk/ri/data',
    models: 'http://vocabs.ceh.ac.uk/ri/model',
    analytics: 'http://vocabs.ceh.ac.uk/ri/analytics',
    software: 'http://vocabs.ceh.ac.uk/ri/software',
    digitalPlatforms: 'http://vocabs.ceh.ac.uk/ri/digitalPlatform'
  },

  classes: {
    instrumentedSites: 'Environmental observatories',
    surveys: 'Environmental observatories',
    wildlifeSchemes: 'Environmental observatories',
    discoveryCollections: 'Environmental observatories',
    mobilePlatforms: 'Environmental observatories',
    controlledPlatforms: 'Environmental experiment platforms',
    fieldPlatforms: 'Environmental experiment platforms',
    labsAnalysis: 'Analytical facilities',
    labsTest: 'Analytical facilities',
    data: 'Digital infrastructures',
    models: 'Digital infrastructures',
    analytics: 'Digital infrastructures',
    software: 'Digital infrastructures',
    digitalPlatforms: 'Digital infrastructures'
  },

  descriptions: {
    instrumentedSites: 'Instrumented sites',
    surveys: 'Surveys',
    wildlifeSchemes: 'Wildlife monitoring schemes',
    discoveryCollections: 'Discovery collections',
    mobilePlatforms: 'Mobile observing platforms',
    controlledPlatforms: 'Controlled environment platforms',
    fieldPlatforms: 'Field research platforms',
    labsAnalysis: 'Analysis labs',
    labsTest: 'Test labs',
    data: 'Environmental data and information',
    models: 'Environmental models',
    analytics: 'Data analystics, method and approaches',
    software: 'Software infrastructure',
    digitalPlatforms: 'Digital computing platforms'
  },

  initialize () {
    this.on('change:value', this.updateLimitation)
  },

  updateLimitation (model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : null)
    this.set('infrastructureClass', this.classes[value] ? this.classes[value] : null)
    this.set('description', this.descriptions[value] ? this.descriptions[value] : null)
  }
})
