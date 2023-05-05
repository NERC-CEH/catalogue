import ObjectInputView from './ObjectInputView'
import _ from 'underscore'
import template from '../templates/Supplemental.tpl'

export default ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    this.$('select.function').val(this.model.get('function'))
    return this
  }
})
