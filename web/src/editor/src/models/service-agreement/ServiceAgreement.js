import { EditorMetadata } from '../../index'
import _ from 'underscore'

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

    if (_.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate because returning something signals a validation error.

    } else {
      return errors
    }
  }
})
