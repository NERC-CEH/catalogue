import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/Category.tpl'
import _ from 'underscore'

export default ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('value'))
    return this
  }
})
