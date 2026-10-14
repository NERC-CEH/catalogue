import _ from 'underscore'
import Backbone from 'backbone'
import { isValidUrl } from '../utils'
export default Backbone.Model.extend({

  validate (attrs) {
    const errors = []

    if (!attrs.funderName) {
      errors.push({
        message:
                    'Funding body is mandatory'
      })
    }

    if (attrs.awardURI && !isValidUrl(attrs.awardURI)) {
      errors.push({
        message:
                    "That URL doesn't look right!"
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
