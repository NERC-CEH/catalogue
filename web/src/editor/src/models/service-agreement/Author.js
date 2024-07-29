import Backbone from 'backbone'
import _ from 'underscore'

export default Backbone.Model.extend({

  defaults: {
    address: {}
  },

  validate ({ organisationName, email, individualName, nameIdentifier }) {
    const emailRegEx = '[a-z0-9!#$%&\'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?'
    const orcidRegEx = '^https?:\\/\\/orcid.org\\/(\\d{4}-){3}\\d{3}[\\dX]$'

    const errors = []

    if (!email?.match(emailRegEx)) {
      errors.push({ message: 'That email address is invalid' })
    }

    if (nameIdentifier && !nameIdentifier?.match(orcidRegEx)) {
      errors.push({ message: 'That ORCiD is invalid.  ORCiDs should be entered as https://orcid.org/0000-1234-5678-999X <b>not</b> 0000-1234-5678-999X' })
    }

    if (!organisationName || !individualName || !email) {
      errors.push({ message: 'Author name, affiliation and email address are mandatory.' })
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
