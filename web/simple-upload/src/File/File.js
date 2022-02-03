import Backbone from 'backbone'
export var File = Backbone.Model.extend({

  defaults: {
    toDelete: false
  },

  idAttribute: 'name'
})
