import { EditorMetadata } from '../../index'

export default EditorMetadata.extend({

  initialize (data, options) {
    EditorMetadata.prototype.initialize.apply(this)
    if ('id' in data) {
      this.id = data.id
      this.parameters = ''
    } else {
      this.id = options.id
      this.parameters = '?catalogue=eidc'
    }
  },

  urlRoot () { return `/service-agreement/${this.id}${this.parameters}` },

  validate (attrs) {
    const errors = EditorMetadata.prototype.validate.call(this, attrs) || []

    if (!attrs?.depositorContactDetails) {
      errors.push('Depositor contact details are mandatory')
    }

    if (attrs.availability) {
      const date = new Date(attrs.availability)
      const today = new Date()
      const maxDate = new Date()
      maxDate.setFullYear(today.getFullYear() + 5)
      today.setHours(0, 0, 0, 0)
      if (date < today || date > maxDate) {
        errors.push('Embargo date is in the past or too far in the future')
      }
    }

    if (errors.length) {
      return errors
    }
  }
})
