import _ from 'underscore'
import template from '../templates/InfrastructureChallenge.tpl'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('value'))
    return this
  }
})
