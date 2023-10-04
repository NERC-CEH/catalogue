import ObjectInputView from './ObjectInputView'
import template from '../templates/ModelResolution'

export default ObjectInputView.extend({
  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select.category').val(this.model.get('category'))
    return this
  }
})
