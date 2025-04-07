import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/rightsHolder'
import { createOrgAutocomplete } from '../../utils'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this)
    createOrgAutocomplete('.orgAutocomplete', this)
  },

  modify (event) {
    ObjectInputView.prototype.modify.call(this, event)
    this.model.set('role', 'rightsHolder')
  }
})
