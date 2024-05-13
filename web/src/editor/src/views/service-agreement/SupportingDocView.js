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
      select (event, { item: { value } }) {
        // Trigger a 'change' event to get the model to update,
        // unfortunately the selected value needs retrieving from the 'select' event first
        this.value = value
        $(this).trigger('change')
      }
    }).on('focus', ({ target: t }) => $(t).autocomplete('search'))

    const model = this.model
    const content = model.get('content') ?? []
    const mkCheckbox = (value, text) => {
      const label = $('<label>', { text })
      const checkbox = $('<input>', {
        type: 'checkbox',
        value,
        checked: content.includes(value),
        on: {
          change () {
            // need to get up-to-date content value here
            // we can't rely on the old value closed over
            const content = model.get('content') ?? []
            if (this.checked) {
              if (!content.includes(this.value)) {
                model.set('content', [...content, this.value])
              }
            } else {
              model.set('content', content.filter(val => val !== this.value))
            }
          }
        }
      })
      label.prepend(checkbox)
      return $('<div>').addClass('checkbox').append(label)
    }

    const mandatory = $('<fieldset>').append($('<legend>').text('Mandatory elements'))
    for (const [value, text] of Object.entries(this.model.mandatoryContentTypes)) {
      mandatory.append(mkCheckbox(value, text))
    }

    const conditional = $('<fieldset>').append(
      $('<legend>').text('Conditional elements (must be included if relevant to the data resource)')
    )
    for (const [value, text] of Object.entries(this.model.conditionalContentTypes)) {
      conditional.append(mkCheckbox(value, text))
    }

    this.$(`#supportingDocs${this.data.index}Content`).append([mandatory, conditional])
  }
})
