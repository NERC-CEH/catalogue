import ObjectInputView from './ObjectInputView'
import template from '../templates/ServiceOperation'

export default ObjectInputView.extend({
  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
  },
  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select.operationName').val(this.model.get('operationName'))
    return this.$('select.platform').val(this.model.get('platform'))
  }
})
