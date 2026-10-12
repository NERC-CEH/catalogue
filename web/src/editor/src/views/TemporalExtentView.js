/* eslint no-new: "off" */
import ObjectInputView from './ObjectInputView'
import template from '../templates/temporalExtent'
import { formatDateForInput } from '../utils'

export default ObjectInputView.extend({
  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)

    const beginDate = formatDateForInput(this.model.get('begin'))
    const endDate = formatDateForInput(this.model.get('end'))
    this.$('[data-name="begin"]').val(beginDate)
    this.$('[data-name="end"]').val(endDate)

    this.$('[data-name="begin"]').on('input', (event) => {
      this.model.set('begin', event.target.value)
    })
    this.$('[data-name="end"]').on('input', (event) => {
      this.model.set('end', event.target.value)
    })
  }
})
