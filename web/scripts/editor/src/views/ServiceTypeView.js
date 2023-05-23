import _ from 'underscore'
import template from '../templates/ServiceType.tpl'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({

  className: 'component component--servicetype visible',

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    return this.$('select').val(this.model.get(this.data.modelAttribute))
  }
})
