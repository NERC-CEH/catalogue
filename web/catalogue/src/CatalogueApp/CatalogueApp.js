import Backbone from 'backbone'

export var CatalogueApp = Backbone.Model.extend({

  url () {
    return this.urlRoot()
  },

  urlRoot () {
    return `/documents/${this.id}/catalogue`
  }
})
