import _ from 'underscore'
import Backbone from 'backbone'
import { isValidUrl } from '../utils'

export default Backbone.Model.extend({

  validate (attrs) {
    const DOIRegEx = /^https?:\/\/doi\.org\/10.\d{4,9}\/[-._;()/:A-Za-z0-9]+$/

    const isValidDOI = doi => DOIRegEx.test(doi)

    const errors = []

    if (attrs.doi && !isValidDOI(attrs.doi)) {
      errors.push({
        message:
                    'Invalid DOI url'
      })
    }

    if (attrs.nora && !isValidUrl(attrs.nora)) {
      errors.push({
        message:
                    'Invalid NORA URL'
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
