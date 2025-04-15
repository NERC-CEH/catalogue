import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    address: {}
  },

  validate ({ organisationName, familyName, givenName }) {
    const errors = []

    if (!organisationName && (!familyName || !givenName)) {
      errors.push({ message: 'You must add either an organisation name or a persons name.' })
    }

    if (organisationName && (familyName || givenName)) {
      errors.push({ message: 'You must add either an organisation name OR a persons name.' })
    }

    if (errors.length) {
      return errors
    }
  }
})
