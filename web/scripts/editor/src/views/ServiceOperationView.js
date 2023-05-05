import ObjectInputView from './ObjectInputView'
import template from '../templates/ServiceOperation.tpl'
import _ from 'underscore'

export default ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    this.$('select.operationName').val(this.model.get('operationName'))
    return this.$('select.platform').val(this.model.get('platform'))
  }
})
