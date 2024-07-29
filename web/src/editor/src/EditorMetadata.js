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

  initialize (data, { mediaType = 'application/json' } = {}, title) {
    this.mediaType = mediaType
    this.title = title
  },

  sync (method, model, options) {
    return Backbone.sync.call(this, method, model, {
      ...options,
      accepts: { json: this.mediaType },
      contentType: this.mediaType
    })
  },

  validate (attrs) {
    const errors = []

    if (!attrs.title) {
      errors.push('A title is mandatory')
    }

    if (errors.length) {
      return errors
    }
  }
})
