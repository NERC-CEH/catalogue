import { ObjectInputView } from '../index'
import $ from 'jquery'
import template from '../../templates/service-agreement/supportingDoc'

export default ObjectInputView.extend({
  template,

  initialize (options) {
    ObjectInputView.prototype.initialize.call(this, options)

    this.$('.autocomplete').autocomplete({
      source: [
        { label: 'docx (preferred)', value: 'docx' },
        { label: 'txt', value: 'txt' },
        { label: 'csv', value: 'csv' }
      ],
      minLength: 0,
      delay: 0,
      select (event, ui) {
        $(this).trigger('change')
      }
    }).on('focus', ({ target: t }) => $(t).autocomplete('search'))
  }
})
