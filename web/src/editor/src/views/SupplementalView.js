import ObjectInputView from './ObjectInputView'
import template from '../templates/supplemental'

export default ObjectInputView.extend({
  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
  },
  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select.function').val(this.model.get('function'))
    return this
  }
})
