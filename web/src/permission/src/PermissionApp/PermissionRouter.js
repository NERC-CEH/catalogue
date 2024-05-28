import Backbone from 'backbone'

export default Backbone.Router.extend({

  routes: {
    'permission/:identifier': 'loadPermission',
    'service-agreement-permission/:identifier': 'loadServiceAgreementPermission'
  },

  initialize (options) {
    this.model = options.model
  },

  loadPermission (identifier) {
    return this.model.loadPermission(identifier)
  },

  loadServiceAgreementPermission (identifier) {
    return this.model.loadServiceAgreementPermission(identifier)
  }
})
