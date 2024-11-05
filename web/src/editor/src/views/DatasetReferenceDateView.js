/* eslint no-new: "off" */
import ObjectInputView from './ObjectInputView'
import template from '../templates/DatasetReferenceDate'

function formatDateForInput (date) {
  if (!date) return ''
  const d = new Date(date)
  return d.toISOString().split('T')[0]
}

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)

    const creationDate = formatDateForInput(this.model.get('creationDate'))
    const publicationDate = formatDateForInput(this.model.get('publicationDate'))
    const unavailableDate = formatDateForInput(this.model.get('unavailableDate'))
    const releasedDate = formatDateForInput(this.model.get('releasedDate'))
    const supersededDate = formatDateForInput(this.model.get('supersededDate'))

    this.$('#input-creationDate').val(creationDate)
    this.$('#input-creationDate').val(creationDate)
    this.$('#input-publicationDate').val(publicationDate)
    this.$('#input-unavailableDate').val(unavailableDate)
    this.$('#input-releasedDate').val(releasedDate)
    this.$('#input-supersededDate').val(supersededDate)

    this.$('#input-creationDate').on('input', (event) => {
      this.model.set('creationDate', this.value)
    })
    this.$('#input-publicationDate').on('input', (event) => {
      this.model.set('publicationDate', event.value)
    })
    this.$('#input-unavailableDate').on('input', (event) => {
      this.model.set('unavailableDate', event.value)
    })
    this.$('#input-releasedDate').on('input', (event) => {
      this.model.set('releasedDate', event.value)
    })
    this.$('#input-supersededDate').on('input', (event) => {
      this.model.set('supersededDate', event.value)
    })
  }
})
