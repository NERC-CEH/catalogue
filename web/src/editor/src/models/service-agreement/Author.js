import Backbone from 'backbone'
import _ from 'underscore'

export default Backbone.Model.extend({

  defaults: {
    address: {}
  },

  validate ({ organisationName, email, familyName, givenName, displayName, nameIdentifier }) {
    const emailRegEx = '^[a-zA-Z0-9!#$%&\'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&\'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?'
    const orcidRegEx = '^https?:\\/\\/orcid.org\\/(\\d{4}-){3}\\d{3}[\\dX]$'

    const errors = []

    if (email && !email?.match(emailRegEx)) {
      errors.push({ message: 'That email address is invalid' })
    }

    if (nameIdentifier && !nameIdentifier?.match(orcidRegEx)) {
      errors.push({ message: 'That ORCiD is invalid.  ORCiDs should be entered as https://orcid.org/0000-1234-5678-999X <b>not</b> 0000-1234-5678-999X' })
    }

    if (!email) {
      errors.push({ message: 'Email address is mandatory' })
    }

    if (!organisationName) {
      errors.push({ message: 'Affiliation is mandatory' })
    }

    if (!displayName && (!familyName || !givenName)) {
      errors.push({ message: 'You must include the author\'s full name OR an institional author name ' })
    }

    if (errors.length) {
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
