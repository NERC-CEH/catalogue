import $ from 'jquery'
import { ObjectInputView } from '../index'
import template, { eachSizeText } from '../../templates/service-agreement/fileGroup'
import { ensureFormatDatalist, setFormat } from '../../utils/formatPicker'
import { showSizeRecognised } from '../../utils/sizeInput'
import { formatBytes } from '../../../../file-size/formatBytes'
import { parseExactSize, parseSize } from '../../../../file-size/parseSize'
import { matchesPattern } from '../../../../hubbub/src/Expected/matchExpected'

export default ObjectInputView.extend({
  template,

  render () {
    ensureFormatDatalist()
    return ObjectInputView.prototype.render.apply(this)
  },

  // The tester box has no data-name, so modify's default case (ObjectInputView.modify) never saves what is typed in it
  events: Object.assign({}, ObjectInputView.prototype.events, {
    'input .pattern-tester': 'tryName',
    'input [data-name="pattern"]': 'tryName'
  }),

  // Says whether the name typed fits the pattern, compared as the Hubbub upload page compares uploads
  tryName () {
    const pattern = this.$('[data-name="pattern"]').val().trim()
    const name = this.$('.pattern-tester').val().trim()
    let result = ''
    if (pattern && name) {
      result = matchesPattern(pattern, name) ? 'matches this pattern' : "doesn't match this pattern"
    }
    this.$('.pattern-tester-result').text(result)
  },

  modify (event) {
    const input = event.target
    switch ($(input).data('name')) {
      case 'count':
        this.modifyCount(input)
        break
      case 'format':
        setFormat(this.model, input)
        break
      case 'eachSize':
        this.modifyEachSize(input)
        break
      case 'totalSize':
        this.modifyTotalSize(input)
        break
      default:
        return ObjectInputView.prototype.modify.call(this, event)
    }
    return false // disable bubbling
  },

  // The count is a whole number in the agreement, so it is kept as one rather than as the typed text
  modifyCount (input) {
    const count = parseInt($(input).val().trim(), 10)
    if (Number.isNaN(count)) {
      this.model.unset('count')
    } else {
      this.model.set('count', count)
    }
  },

  // An exact size is stored as a range of one. The older agreement's size text is shown in this box,
  // so it is replaced only from here, and kept when the new text is not a size.
  modifyEachSize (input) {
    const text = $(input).val().trim()
    const size = parseSize(text)
    showSizeRecognised(input, !text || size !== null)
    if (text && size === null) return
    if (size === null) {
      this.model.unset('minBytes')
      this.model.unset('maxBytes')
    } else {
      const min = size.bytes ?? size.min
      const max = size.bytes ?? size.max
      this.model.set({ minBytes: min, maxBytes: max })
      $(input).val(eachSizeText(min, max))
    }
    this.model.unset('size')
  },

  modifyTotalSize (input) {
    const text = $(input).val().trim()
    const bytes = parseExactSize(text)
    showSizeRecognised(input, !text || bytes !== null)
    if (text && bytes === null) return
    if (bytes === null) {
      this.model.unset('totalBytes')
    } else {
      this.model.set('totalBytes', bytes)
      $(input).val(formatBytes(bytes))
    }
  }
})
