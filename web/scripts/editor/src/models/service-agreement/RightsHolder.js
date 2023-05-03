import Backbone from 'backbone'
import _ from 'underscore'

export default Backbone.Model.extend({

  defaults: {
    address: {}
  },

  validate (attrs) {
    const rorRegEx = new RegExp(`
^https?:\\/\\/ror.org\\/\\w{8,10}$
`)

    const errors = []

    const {
      organisationName
    } = attrs
    const {
      organisationIdentifier
    } = attrs

    const isValidROR = id => rorRegEx.test(id)

    if (organisationIdentifier && !isValidROR(organisationIdentifier)) {
      errors.push({
        message:
          'That RoR is invalid '
      })
    }

    if (!organisationName) {
      errors.push({ message: 'Affiliation (organisation name) is  mandatory.' })
    }

    if (_.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
    } else {
      return errors
    }
  }
})
