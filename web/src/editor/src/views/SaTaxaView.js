import ObjectInputView from './ObjectInputView'
import template from '../templates/SaTaxa'

export default ObjectInputView.extend({
  initialize () {
    this.template = template
  },
  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('value'))
    return this
  }
})
