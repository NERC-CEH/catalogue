import Backbone from 'backbone'

export default Backbone.Router.extend({

  routes: {
    'permission/:identifier': 'loadPermission'
  },

  initialize (options) {
    this.model = options.model
  },

  loadPermission (identifier) {
    return this.model.loadPermission(identifier)
  }
})
