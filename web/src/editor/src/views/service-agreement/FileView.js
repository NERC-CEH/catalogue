import $ from 'jquery'
import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/file'
import { ensureFormatDatalist, setFormat } from '../../utils/formatPicker'
import { showSizeRecognised } from '../../utils/sizeInput'
import { formatBytes } from '../../../../file-size/formatBytes'
import { parseExactSize } from '../../../../file-size/parseSize'

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
