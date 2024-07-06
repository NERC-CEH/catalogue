import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    address: {}
  },

  validate ({ organisationName, organisationIdentifier }) {
    const rorRegEx = /^https?:\/\/ror.org\/\w{8,10}$/

    const errors = []

    if (organisationIdentifier && !organisationIdentifier?.match(rorRegEx)) {
      errors.push({ message: 'That RoR is invalid' })
    }

    if (!organisationName) {
      errors.push({ message: 'Affiliation (organisation name) is mandatory.' })
    }

    if (errors.length) {
      return errors
    }
  }
})
