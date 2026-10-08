import ObjectInputView from './ObjectInputView'
import template from '../templates/Note'

export default ObjectInputView.extend({

  initialize () {
    this.template = template

    if (!this.model.get('addedDateTime')) {
      this.model.set('addedDateTime', new Date().toISOString().split('T')[0])
    }

    ObjectInputView.prototype.initialize.apply(this)
  }
})
