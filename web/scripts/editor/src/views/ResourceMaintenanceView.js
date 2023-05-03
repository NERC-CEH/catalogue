import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import template from '../templates/ResourceMaintenance.tpl'

export default ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('frequencyOfUpdate'))
    return this
  }
})
