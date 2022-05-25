import template from '../templates/InfrastructureOnlineLink.tpl'
import ObjectInputView from './ObjectInputView'
import _ from 'underscore'

ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    return this.$('select.function').val(this.model.get('function'))
  }
})
