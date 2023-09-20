import template from '../templates/ServiceType'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({

  className: 'component component--servicetype visible',

  initialize () {
    this.template = template
  },
  render () {
    ObjectInputView.prototype.render.apply(this)
    return this.$('select').val(this.model.get(this.data.modelAttribute))
  }
})
