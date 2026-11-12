import Backbone from 'backbone'
import _ from 'underscore'
import { personIdentifierError } from '../utils/orcid'

export default Backbone.Model.extend({

  defaults: {
    address: {}
  },

  validate (attrs) {
    const emailRegEx = '[a-z0-9!#$%&\'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?'
    const rorRegEx = '^https?:\\/\\/ror.org\\/\\w{8,10}$'

    const errors = []

    const {
      organisationName
    } = attrs
    const {
      organisationIdentifier
    } = attrs
    const {
      role
    } = attrs
    const {
      email
    } = attrs
    const {
      nameIdentifier
    } = attrs

    if (email && !email.match(emailRegEx)) {
      errors.push({ message: 'That email address is wrong' })
    }

    // One message, saying what's wrong: a bare iD is stored in full as it's typed (ContactView), so
    // only a wrong check character or something that isn't an iD at all is left to say (dri-one #534)
    const identifierError = personIdentifierError(nameIdentifier)
    if (identifierError) {
      errors.push({ message: identifierError })
    }

    if (organisationIdentifier && !organisationIdentifier.match(rorRegEx)) {
      errors.push({ message: 'Invalid RoR' })
    }

    if (!role) {
      errors.push({ message: 'Role is mandatory.' })
    }

    if (!organisationName) {
      errors.push({ message: 'Organisation name is mandatory.' })
    }

    if (_.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
      return null
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
