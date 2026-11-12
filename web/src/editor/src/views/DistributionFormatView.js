import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import template from '../templates/DistributionFormat'
import { ensureFormatDatalist, pickFormat } from '../utils/formatPicker'

export default ObjectInputView.extend({
  template,

  render () {
    ensureFormatDatalist()
    return ObjectInputView.prototype.render.apply(this)
  },

  // A listed format, or an extension of one, brings its media type; anything else has none to offer
  modify (event) {
    if ($(event.target).data('name') !== 'name') {
      return ObjectInputView.prototype.modify.call(this, event)
    }
    const { name, type } = pickFormat($(event.target).val())
    if (name) {
      $(event.target).val(name)
      this.model.set('name', name)
      if (type) this.model.set('type', type)
      else this.model.unset('type')
    } else {
      this.model.unset('name')
      this.model.unset('type')
    }
    return false // disable bubbling
  }
})
