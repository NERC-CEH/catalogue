/* eslint no-new: "off" */
import ObjectInputView from './ObjectInputView'
import template from '../templates/temporalExtent'

function formatDateForInput (date) {
  if (!date) return ''
  const d = new Date(date)
  return d.toISOString().split('T')[0]
}

export default ObjectInputView.extend({
  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
  },

  render () {
    ObjectInputView.prototype.render.call(this)

    const beginDate = formatDateForInput(this.model.get('begin'))
    const endDate = formatDateForInput(this.model.get('end'))
    this.$('#input-begin').val(beginDate)
    this.$('#input-end').val(endDate)

    this.$('#input-begin').on('input', (event) => {
      this.model.set('begin', this.value)
    })
    this.$('#input-end').on('input', (event) => {
      this.model.set('end', this.value)
    })
  }
})
