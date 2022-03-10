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
      value: ''
    },

    uris: {
      'Air dried': 'http://vocab.ceh.ac.uk/esb#airdry',
      'Chemical extract': 'http://vocab.ceh.ac.uk/esb#chemicalExtract',
      'Chilled (refrigerated)': 'http://vocab.ceh.ac.uk/esb#chilled',
      'Fixed in formalin': 'http://vocab.ceh.ac.uk/esb#formalinFixed',
      'Formalin-Fixed Paraffin-Embedded (FFPE) tissue': 'http://vocab.ceh.ac.uk/esb#formalin-paraffin',
      'Freeze dried': 'http://vocab.ceh.ac.uk/esb#freezeDried',
      Fresh: 'http://vocab.ceh.ac.uk/esb#fresh',
      'Frozen (-198 degrees C)': 'http://vocab.ceh.ac.uk/esb#frozen-198',
      'Frozen (-20 degrees C)': 'http://vocab.ceh.ac.uk/esb#frozen-20',
      'Frozen (-80 degrees C)': 'http://vocab.ceh.ac.uk/esb#frozen-80',
      'Natural state': 'http://vocab.ceh.ac.uk/esb#naturalState',
      'Oven dry': 'http://vocab.ceh.ac.uk/esb#ovendry',
      Preserved: 'http://vocab.ceh.ac.uk/esb#preserved',
      'Preserved in alcohol': 'http://vocab.ceh.ac.uk/esb#preserved-in-alcohol',
      Slide: 'http://vocab.ceh.ac.uk/esb#slide',
      Taxidermy: 'http://vocab.ceh.ac.uk/esb#taxidermy',
      'Under liquid nitrogen': 'http://vocab.ceh.ac.uk/esb#liquidnitrogen'
    },

    initialize () {
      return this.on('change:value', this.updateUri)
    },

    updateUri (model, value) {
      return this.set('uri', this.uris[value] ? this.uris[value] : '')
    }
  })
})
