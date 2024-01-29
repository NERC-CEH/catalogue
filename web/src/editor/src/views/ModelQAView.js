import ObjectInputView from './ObjectInputView'
import template from '../templates/ModelQA'

export default ObjectInputView.extend({

  render () {
    this.template = template
    ObjectInputView.prototype.render.apply(this)
    this.$('select.category').val(this.model.get('category'))
    return this
  }
})
