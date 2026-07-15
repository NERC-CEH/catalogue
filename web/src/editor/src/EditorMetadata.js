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
    this._revision = null
  },

  setRevision (revision) {
    this._revision = revision
  },

  getRevision () {
    return this._revision
  },

  sync (method, model, options) {
    const headers = { ...(options.headers || {}) }
    if (method === 'update' && this._revision) {
      headers['If-Match'] = this._revision
    }
    const xhr = Backbone.sync.call(this, method, model, {
      ...options,
      headers,
      accepts: { json: this.mediaType },
      contentType: this.mediaType
    })
    // Refresh the stored revision from the response ETag so a multi-save session never self-conflicts.
    if (xhr && xhr.done) {
      xhr.done((data, status, jqXHR) => {
        const etag = jqXHR && jqXHR.getResponseHeader && jqXHR.getResponseHeader('ETag')
        if (etag) {
          this._revision = etag.replace(/^"|"$/g, '')
        }
      })
    }
    return xhr
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
