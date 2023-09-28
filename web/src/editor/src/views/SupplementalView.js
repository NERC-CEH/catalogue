import ObjectInputView from './ObjectInputView'
import template from '../templates/supplemental'

export default ObjectInputView.extend({
  initialize () {
    this.template = template
  },
  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select.function').val(this.model.get('function'))
    return this
  }
})
