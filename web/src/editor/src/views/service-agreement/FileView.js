import $ from 'jquery'
import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/file'
import { cleanText } from '../../utils'
import { ensureFormatDatalist, pickFormat } from '../../utils/formatPicker'
import { formatBytes } from '../../../../file-size/formatBytes'
import { parseExactSize } from '../../../../file-size/parseSize'

const SIZE_NOT_RECOGNISED = 'Size not recognised, e.g. 32 MB or 2-2.5 MB'

/** A listed format, or an extension of one, brings its media type; anything else has none to offer. */
export function setFormat (model, input) {
  const { name, type } = pickFormat(cleanText($(input).val().trim()))
  $(input).val(name)
  if (name) model.set('format', name)
  else model.unset('format')
  if (type) model.set('formatType', type)
  else model.unset('formatType')
}

/** Shows, or clears, the message beside a size box that could not be read. */
export function showSizeRecognised (input, recognised) {
  $(input).siblings('.size-error').text(recognised ? '' : SIZE_NOT_RECOGNISED).prop('hidden', recognised)
}

export default ObjectInputView.extend({
  template,

  render () {
    ensureFormatDatalist()
    return ObjectInputView.prototype.render.apply(this)
  },

  modify (event) {
    const input = event.target
    switch ($(input).data('name')) {
      case 'format':
        setFormat(this.model, input)
        return false // disable bubbling
      case 'sizeEntry':
        this.modifySize(input)
        return false // disable bubbling
      default:
        return ObjectInputView.prototype.modify.call(this, event)
    }
  },

  // The exact bytes are stored and only shown formatted, so other edits never round them. Text that
  // is not a size changes nothing, so an older agreement's size text is kept until it is replaced.
  modifySize (input) {
    const text = $(input).val().trim()
    const bytes = parseExactSize(text)
    showSizeRecognised(input, !text || bytes !== null)
    if (text && bytes === null) return
    if (bytes === null) {
      this.model.unset('bytes')
    } else {
      this.model.set('bytes', bytes)
      $(input).val(formatBytes(bytes))
    }
    this.model.unset('size')
  }
})
