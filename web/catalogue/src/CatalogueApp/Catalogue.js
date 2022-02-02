import Backbone from 'backbone'

export var Catalogue = Backbone.Model.extend({

  url () {
    return this.urlRoot()
  },

  urlRoot () {
    return `/documents/${this.id}/catalogue`
  }
})
