import Backbone from 'backbone'
import _ from 'underscore'
import { IdentityPermissions } from '../IdentityPermission'

export default Backbone.Model.extend({

  url () {
    return this.urlRoot()
  },

  urlRoot () {
    return `/${this.get('doctype')}/${this.id}/permission`
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

    // Capture the revision by wrapping `options.success` (invoked directly by
    // jQuery.ajax as the raw (data, status, jqXHR) callback) rather than by
    // chaining onto the xhr returned from Backbone.sync. jQuery.ajax attaches
    // `options.success` as the FIRST done-callback on the promise, internally,
    // before ever returning the xhr/promise to this method - so anything
    // chained afterwards via `xhr.done(...)` always fires SECOND, i.e. after
    // Backbone's own wrapped success (set up by fetch()/save()), which is what
    // synchronously triggers PermissionApp's 'loaded' event and, in turn,
    // PermissionAppView's clone of this model via getPermission(). Capturing
    // here instead ensures `_revision` is already set before that clone happens.
    const originalSuccess = options.success
    const wrappedOptions = {
      ...options,
      headers,
      success: (data, status, jqXHR) => {
        const etag = jqXHR && jqXHR.getResponseHeader && jqXHR.getResponseHeader('ETag')
        if (etag) {
          this._revision = etag.replace(/^"|"$/g, '')
        }
        if (originalSuccess) {
          originalSuccess(data, status, jqXHR)
        }
      }
    }

    return Backbone.sync.call(this, method, model, wrappedOptions)
  },

  loadCollection () {
    const collection = new IdentityPermissions()
    collection.reset(this.get('permissions'))
    this.set('permissions', collection)
  },

  addPermission (permission) {
    const collection = _.clone(this.get('permissions'))
    collection.add(permission)
    this.set('permissions', collection)
    this.trigger('permission:add')
  },

  removePermission (permission) {
    const collection = _.clone(this.get('permissions'))
    collection.remove(permission)
    this.set('permissions', collection)
    this.trigger('permission:remove')
  }
})
