import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/endUserLicence'

export default ObjectInputView.extend({

  events: {
    'change .ogl': 'setOgl',
    'change .other': 'setOther',
    'change .value': 'setValue',
    'change .uri': 'setUri'
  },

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
    this.$resourceConstraint = this.$('.resourceConstraint')
    const hasUri = this.model.has('uri')
    const hasValue = this.model.has('value')

    if (hasUri || hasValue) {
      if (hasUri && (this.model.get('uri') === 'https://eidc.ceh.ac.uk/licences/OGL/plain')) {
        this.$('input.ogl').prop('checked', true)
      } else {
        this.$('input.other').prop('checked', true)
        this.$resourceConstraint.removeClass('hidden')
        if (hasValue) {
          this.$('.value').val(this.model.get('value'))
        }
      }
    } else {
      this.$('input.ogl').prop('checked', true).change()
    }
  },

  setOgl () {
    this.$resourceConstraint.addClass('hidden')
    this.model.set({
      value: 'This resource is available under the terms of the Open Government Licence',
      code: 'license',
      uri: 'https://eidc.ceh.ac.uk/licences/OGL/plain'
    })
  },

  setOther () {
    this.$resourceConstraint.removeClass('hidden')
    this.model.unset('uri')
    this.model.unset('value')
  },

  setValue (event) {
    this.model.set({
      code: 'license',
      value: event.target.value
    })
  }
})
