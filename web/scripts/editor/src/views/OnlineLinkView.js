import _ from 'underscore'
import template from '../templates/ObservationCapability.tpl'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({

  initialize () {
    this.template = _.template(template)

    ObjectInputView.prototype.initialize.apply(this)
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    return this
  }
})
