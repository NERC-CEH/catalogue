import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/author'

export default ObjectInputView.extend({

  initialize () {
    this.template = template
    ObjectInputView.prototype.initialize.call(this)
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
