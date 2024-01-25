import Backbone from 'backbone'
import _ from 'underscore'

export default Backbone.Model.extend({

  defaults: {
    address: {}
  },

  validate (attrs) {
    const emailRegEx = '[a-z0-9!#$%&\'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?'
    const urlRegEx = '[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)'
    const orcidRegEx = '^https?:\\/\\/orcid.org\\/(\\d{4}-){3}\\d{3}[\\dX]$'
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
      errors.push({
        message:
                    'That email address is wrong'
      })
    }

    if (nameIdentifier && !nameIdentifier.match(orcidRegEx)) {
      errors.push({
        message:
                    "If that's supposed to be an ORCiD, it's not quite right!"
      })
    }

    if (nameIdentifier && !nameIdentifier.match(urlRegEx)) {
      errors.push({
        message:
                    'Are you using the <i>fully-qualified</i> name identifier. For example, ORCiDs should be entered as https://orcid.org/0000-1234-5678-999X <b>not</b> 0000-1234-5678-999X'
      })
    }

    if (organisationIdentifier && !organisationIdentifier.match(rorRegEx)) {
      errors.push({
        message:
                    "If that's supposed to be an ROR, it's not quite right!"
      })
    }

    if (!organisationName || !role) {
      errors.push({ message: 'The organisation name and role are mandatory.' })
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
