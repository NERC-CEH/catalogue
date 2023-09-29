import template from '../templates/InfrastructureChallenge'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({

  render () {
    this.template = template
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('value'))
    return this
  }
})
