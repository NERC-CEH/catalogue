import $ from 'jquery'
import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/fileGroup'

export default ObjectInputView.extend({
  template,

  // The count is a whole number in the agreement, so it is kept as one rather than as the typed text
  modify (event) {
    if ($(event.target).data('name') !== 'count') {
      return ObjectInputView.prototype.modify.call(this, event)
    }
    const count = parseInt($(event.target).val().trim(), 10)
    if (Number.isNaN(count)) {
      this.model.unset('count')
    } else {
      this.model.set('count', count)
    }
    return false // disable bubbling
  }
})
