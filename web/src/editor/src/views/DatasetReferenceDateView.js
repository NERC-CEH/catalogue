/* eslint no-new: "off" */
import ObjectInputView from './ObjectInputView'
import template from '../templates/DatasetReferenceDate'
import { formatDateForInput } from '../utils'

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
    this.$('#input-publicationDate').val(publicationDate)
    this.$('#input-unavailableDate').val(unavailableDate)
    this.$('#input-releasedDate').val(releasedDate)
    this.$('#input-supersededDate').val(supersededDate)
  }
})
