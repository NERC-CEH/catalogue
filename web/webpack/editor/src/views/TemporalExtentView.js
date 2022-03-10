import ObjectInputView from './ObjectInputView'
import template from '../templates/TemporalExtent.tpl'
import _ from 'underscore'

export default ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    this.$('input').datepicker({ dateFormat: 'yy-mm-dd' })
    return this
  }
})
