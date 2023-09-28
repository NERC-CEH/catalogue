import ObjectInputView from './ObjectInputView'
import template from '../templates/supplementalEIDC'
export default ObjectInputView.extend({

  render () {
    this.template = template
    ObjectInputView.prototype.render.apply(this)
    this.$('select.function').val(this.model.get('function'))
    return this
  }
})
