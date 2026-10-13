import Backbone from 'backbone'

const COORDINATES = ['westBoundLongitude', 'eastBoundLongitude', 'southBoundLatitude', 'northBoundLatitude']

// A single box is held as a Backbone model once edited (SingleObjectView), a list as plain objects
const asJSON = box => typeof box?.toJSON === 'function' ? box.toJSON() : box
const boundingBoxes = attrs => [attrs.boundingBox, ...(attrs.boundingBoxes || [])].filter(Boolean).map(asJSON)
const coordinatesSet = box => COORDINATES.filter(name => box[name] != null && box[name] !== '').length

export { COORDINATES, asJSON, coordinatesSet }

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

    if (boundingBoxes(attrs).some(box => coordinatesSet(box) > 0 && coordinatesSet(box) < COORDINATES.length)) {
      errors.push('A bounding box needs all four coordinates: west, east, south and north')
    }

    if (errors.length) {
      return errors
    }
  }
})
