import template from '../templates/OnlineLink'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({
  initialize () {
    this.template = template
  },
  render () {
    ObjectInputView.prototype.render.apply(this)
    return this
  }
})
