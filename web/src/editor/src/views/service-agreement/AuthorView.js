import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/author'
import { createOrgAutocomplete } from '../../utils'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this)
    createOrgAutocomplete('.orgAutocomplete', this)
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select.honorificPrefix').val(this.model.get('honorificPrefix'))
    return this
  },

  modify (event) {
    ObjectInputView.prototype.modify.call(this, event)
    this.model.set('role', 'author')
  }
})
