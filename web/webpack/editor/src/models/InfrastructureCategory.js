/* eslint-disable
    no-undef,
*/
// TODO: This file was created by bulk-decaffeinate.
// Fix any style issues and re-enable lint.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone'
], function (Backbone) {
  return Backbone.Model.extend({

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
      labsTest: 'http://vocabs.ceh.ac.uk/ri/labsTest'
    },

    classes: {
      instrumentedSites: 'Environmental observatories',
      surveys: 'Environmental observatories',
      wildlifeSchemes: 'Environmental observatories',
      discoveryCollections: 'Environmental observatories',
      mobilePlatforms: 'Environmental observatories',
      controlledPlatforms: 'Environmental experiment platforms',
      fieldPlatforms: 'Environmental experiment platforms',
      labsAnalysis: 'Environmental analysis',
      labsTest: 'Environmental analysis'
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
      labsTest: 'Test labs'
    },

    initialize () {
      return this.on('change:value', this.updateLimitation)
    },

    updateLimitation (model, value) {
      this.set('uri', this.uris[value] ? this.uris[value] : null)
      this.set('infrastructureClass', this.classes[value] ? this.classes[value] : null)
      return this.set('description', this.descriptions[value] ? this.descriptions[value] : null)
    }
  })
})
