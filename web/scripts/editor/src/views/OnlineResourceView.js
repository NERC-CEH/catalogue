import ObjectInputView from './ObjectInputView'
import template from '../templates/OnlineResource.tpl'
import _ from 'underscore'

export default ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('function'))
    return this
  }
})
