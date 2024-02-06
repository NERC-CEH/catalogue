import template from '../templates/OnlineLink'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({
  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
  },
  render () {
    ObjectInputView.prototype.render.apply(this)
    return this
  }
})
