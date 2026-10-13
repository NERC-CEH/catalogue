import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    metricsReport: []
  },

  searchFields: ['startDate', 'endDate', 'orderBy', 'ordering', 'recordType', 'docId', 'noOfRecords'],

  initialize () {
    // One search per change, however many fields it touched: a listener per field fetched once
    // for each (dri-one #461)
    this.on('change', model => {
      if (this.searchFields.some(field => field in model.changed)) this.doSearch()
    })
  },

  url () { return window.location.pathname },

  doSearch (model) {
    this.fetch({ cache: false, traditional: true, data: this.getState() })
  },

  getState () {
    return Object.fromEntries(
      Object.entries(this.attributes)
        .filter(([key, val]) => val && this.searchFields.includes(key))
    )
  },

  // required for compatibility with SearchRouter
  setState (state, options) {
    this.set(state, options)
  }
})
