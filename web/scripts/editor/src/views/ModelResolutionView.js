import ObjectInputView from './ObjectInputView'
import template from '../templates/ModelResolution.tpl'
import _ from 'underscore'

export default ObjectInputView.extend({
  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    this.$('select.category').val(this.model.get('category'))
    return this
  }
})
