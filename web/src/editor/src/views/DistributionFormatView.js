import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import template from '../templates/DistributionFormat'
import { cleanText } from '../utils'
import { ensureFormatDatalist, pickFormat } from '../utils/formatPicker'
import { FORMATS, findFormat } from '../formats'

const isListedType = type => Boolean(type) && FORMATS.some(f => f.type === type)

export default ObjectInputView.extend({
  template,

  render () {
    ensureFormatDatalist()
    return ObjectInputView.prototype.render.apply(this)
  },

  // A listed format, or an extension of one, brings its media type. An unlisted name drops a type the
  // list filled in, but keeps one entered some other way (the API or an import), as the box is read-only.
  modify (event) {
    if ($(event.target).data('name') !== 'name') {
      return ObjectInputView.prototype.modify.call(this, event)
    }
    const { name, type } = pickFormat(cleanText($(event.target).val().trim()))
    if (name) {
      $(event.target).val(name)
      this.model.set('name', name)
      if (type) this.model.set('type', type)
      else if (findFormat(name) || isListedType(this.model.get('type'))) this.model.unset('type')
    } else {
      this.model.unset('name')
      this.model.unset('type')
    }
    this.$('[data-name="type"]').val(this.model.get('type') ?? '')
    return false // disable bubbling
  }
})
