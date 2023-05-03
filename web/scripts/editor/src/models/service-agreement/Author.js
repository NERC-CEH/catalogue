import Backbone from 'backbone'
import _ from 'underscore'

export default Backbone.Model.extend({

  defaults: {
    address: {}
  },

  validate (attrs) {
    const emailRegEx = '[a-z0-9!#$%&\'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?'
    const orcidRegEx = '^https?:\\/\\/orcid.org\\/(\\d{4}-){3}\\d{3}[\\dX]$'

    const errors = []

    const {
      organisationName
    } = attrs
    const {
      email
    } = attrs
    const {
      individualName
    } = attrs
    const {
      nameIdentifier
    } = attrs

    const isValidEmail = address => address.match(emailRegEx)

    const isValidORCID = id => id.match(orcidRegEx)

    if (email && !isValidEmail(email)) {
      errors.push({
        message:
          'That email address is invalid'
      })
    }

    if (nameIdentifier && !isValidORCID(nameIdentifier)) {
      errors.push({
        message:
          'That ORCiD is invalid.  ORCiDs should be entered as https://orcid.org/0000-1234-5678-999X <b>not</b> 0000-1234-5678-999X'
      })
    }

    if (!organisationName || !individualName || !email) {
      errors.push({ message: 'Author name, affiliation and email address are mandatory.' })
    }

    if (_.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate
      // because returning something signals a validation error.

    } else {
      return errors
    }
  },

  toJSON () {
    if (_.isEmpty(this.get('address'))) {
      return _.omit(this.attributes, 'address')
    } else {
      return _.clone(this.attributes)
    }
  }
})
