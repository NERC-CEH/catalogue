import Backbone from 'backbone'
import _ from 'underscore'
import Permission from './Permission'

const DOC_TYPE_DOCUMENTS = 'documents'
const DOC_TYPE_SERVICE_AGREEMENT = 'service-agreement'

export default Backbone.Model.extend({

  loadServiceAgreementPermission (identifier) {
    return this.loadPermission(identifier, DOC_TYPE_SERVICE_AGREEMENT)
  },

  loadPermission (identifier, doctype = DOC_TYPE_DOCUMENTS) {
    const permission = new Permission({
      id: identifier,
      doctype
    })

    return permission.fetch({
      success: model => {
        model.loadCollection()
        this.set('permission', model)
        this.trigger('loaded')
      },

      error: model => {
        this.trigger('error', `Unable to load permission for: ${model.id}`)
      }
    })
  },

  getPermission () {
    return _.clone(this.get('permission'))
  }
})
