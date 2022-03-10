import Backbone from 'backbone'

export default Backbone.Model.extend({

    defaults: {
      value: ''
    },

    uris: {
      dataset: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/dataset',
      series: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/series',
      service: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/services'
    },

    initialize () {
      return this.on('change:value', this.updateUri)
    },

    updateUri (model, value) {
      return this.set('uri', this.uris[value] ? this.uris[value] : '')
    }
})
