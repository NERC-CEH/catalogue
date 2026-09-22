import Backbone from 'backbone'

export default Backbone.Model.extend({

  url () {
    return this.urlRoot()
  },

  urlRoot () {
    return `/documents/${this.id}/catalogue-view`
  },

  initialize () {
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
      headers
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
  }
})
