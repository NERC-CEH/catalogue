import ObjectInputView from './ObjectInputView'
import template from '../templates/InspireTheme'

export default ObjectInputView.extend({

  initialize () {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this)
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select.theme').val(this.model.get('theme'))
    this.$('select.conformity').val(this.model.get('conformity'))
    return this
  }
})
