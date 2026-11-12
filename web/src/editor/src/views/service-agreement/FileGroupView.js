import $ from 'jquery'
import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/fileGroup'
import { matchesPattern } from '../../../../hubbub/src/Expected/matchExpected'

export default ObjectInputView.extend({
  template,

  // The tester box has no data-name, so ObjectInputView.modify never saves what is typed in it
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
