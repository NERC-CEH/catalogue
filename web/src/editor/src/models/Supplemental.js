import _ from 'underscore'
import Backbone from 'backbone'
import { isValidUrl } from '../utils'

export default Backbone.Model.extend({

  validate (attrs) {
    const errors = []

    if (attrs.url && !isValidUrl(attrs.url)) {
      errors.push({
        message:
                    "That url isn't right."
      })
    }

    if (_.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate
      // because returning something signals a validation error.

    } else {
      return errors
    }
  }
})
