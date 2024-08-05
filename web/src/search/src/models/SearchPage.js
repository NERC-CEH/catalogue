import Backbone from 'backbone'

export default Backbone.Model.extend({

  url () { return window.location.pathname },

  defaults: {
    results: [],
    prevPage: null,
    nextPage: null
  }
})
