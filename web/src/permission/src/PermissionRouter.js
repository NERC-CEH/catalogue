import Backbone from 'backbone'

export var PermissionRouter = Backbone.Router.extend({

  routes: {
    'permission/:identifier': 'loadPermission'
  },

  initialize(options) {
    return this.model = options.model;
  },

  loadPermission(identifier) {
    return this.model.loadPermission(identifier);
  }
});