import _ from 'underscore'
import template from '../templates/ObservationCapability.tpl'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    return this
  }
})
