import _ from 'underscore'
import Backbone from 'backbone'

export default Backbone.Model.extend({

  url () {
    return this.urlRoot()
  },

  urlRoot () {
    if (this.isNew()) {
      return `/documents?catalogue=${window.location.pathname.split('/')[1]}`
    } else {
      return `/documents/${this.id}`
    }
  },

  initialize () {
    if (arguments.length > 1) {
      this.mediaType = arguments[1].mediaType
      this.title = arguments[2]
    } else {
      this.mediaType = 'application/json'
    }
  },

  sync (method, model, options) {
    return Backbone.sync.call(this, method, model, _.extend(options, {
      accepts: {
        json: this.mediaType
      },
      contentType: this.mediaType
    }))
  },

  validate (attrs) {
    const errors = []
    if ((attrs != null ? attrs.title : undefined) == null) {
      errors.push('A title is mandatory')
    }

    if (_.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate because returning something signals a validation error.

    } else {
      return errors
    }
  }
})
