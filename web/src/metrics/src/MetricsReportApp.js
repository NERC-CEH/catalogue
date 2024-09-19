import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    metricsReport: []
  },

  searchFields: ['startDate', 'endDate', 'orderBy', 'ordering', 'recordType', 'docId', 'noOfRecords'],

  initialize () {
    for (const field of this.searchFields) {
      this.on(`change:${field}`, this.doSearch)
    }
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
