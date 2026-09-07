import Backbone from 'backbone'
import { Permission, PermissionApp } from '../src/PermissionApp'

describe('Test Permission', () => {
  it('Should have doctype in urlRoot', () => {
    // given
    const id = 'myId'
    const doctype = 'myDoctype'
    const model = { doctype, id }
    const permission = new Permission(model)

    // when
    const result = permission.urlRoot()

    // then
    const expected = `/${doctype}/${id}/permission`
    expect(result).toEqual(expected)
  })

  describe('optimistic locking', () => {
    let syncArgs

    beforeEach(() => {
      spyOn(Backbone, 'sync').and.callFake((method, model, options) => {
        syncArgs = { method, options }
        return { done: () => {} }
      })
    })

    it('sends If-Match header on update when a revision is set', () => {
      const permission = new Permission({ id: 'doc1', doctype: 'documents' })
      permission.setRevision('rev1')
      permission.save()
      expect(syncArgs.method).toBe('update')
      expect(syncArgs.options.headers['If-Match']).toBe('rev1')
    })

    it('does not send If-Match on update when no revision is set', () => {
      const permission = new Permission({ id: 'doc1', doctype: 'documents' })
      permission.save()
      const headers = syncArgs.options.headers || {}
      expect(headers['If-Match']).toBeUndefined()
    })

    it('does not send If-Match on read even when a revision is set', () => {
      const permission = new Permission({ id: 'doc1', doctype: 'documents' })
      permission.setRevision('rev1')
      permission.fetch()
      expect(syncArgs.method).toBe('read')
      const headers = syncArgs.options.headers || {}
      expect(headers['If-Match']).toBeUndefined()
    })

    it('merges caller-supplied headers rather than clobbering them', () => {
      const permission = new Permission({ id: 'doc1', doctype: 'documents' })
      permission.setRevision('rev1')
      permission.save({}, { headers: { 'X-Custom': 'value' } })
      expect(syncArgs.options.headers['X-Custom']).toBe('value')
      expect(syncArgs.options.headers['If-Match']).toBe('rev1')
    })
  })

  describe('revision refresh from ETag', () => {
    let capturedOptions

    beforeEach(() => {
      // Mirrors the real jQuery.ajax contract: `options.success` is the raw
      // (data, status, jqXHR) callback jQuery invokes directly on a
      // successful response - it is not something reached via a separately
      // chained xhr.done(...).
      spyOn(Backbone, 'sync').and.callFake((method, model, options) => {
        capturedOptions = options
        return { done: () => {} }
      })
    })

    it('stores the de-quoted response ETag as the new revision after a successful sync', () => {
      const permission = new Permission({ id: 'doc1', doctype: 'documents' })
      permission.setRevision('rev1')
      permission.save()

      const jqXHR = {
        getResponseHeader: (name) => (name === 'ETag' ? '"rev2"' : null)
      }
      capturedOptions.success({}, 'success', jqXHR)

      expect(permission.getRevision()).toBe('rev2')
    })

    it('still invokes the caller-supplied success callback after capturing the revision', () => {
      const permission = new Permission({ id: 'doc1', doctype: 'documents' })
      let successCalledWith
      permission.save({}, {
        success: (model, resp, options) => { successCalledWith = { model, resp, options } }
      })

      const jqXHR = {
        getResponseHeader: (name) => (name === 'ETag' ? '"rev2"' : null)
      }
      capturedOptions.success({ id: 'doc1', doctype: 'documents' }, 'success', jqXHR)

      expect(permission.getRevision()).toBe('rev2')
      expect(successCalledWith).toBeDefined()
      expect(successCalledWith.model).toBe(permission)
    })
  })

  describe('clone risk: revision must survive PermissionApp.getPermission()', () => {
    // PermissionApp.loadPermission() fetches via Backbone.Model#fetch, whose
    // internal wrapped `success` option is what actually triggers the
    // 'loaded' event (synchronously, from inside the ajax success callback).
    // PermissionAppView.render() clones the model at that exact point via
    // getPermission() (_.clone). This fake mirrors the real jQuery.ajax
    // contract precisely: `options.success` is registered as the FIRST
    // done-callback, synchronously, BEFORE the xhr/promise is returned to
    // the caller - exactly as $.ajax does internally before Backbone.sync
    // gets the jqXHR back. Any callback added to xhr.done(...) afterwards
    // (as Permission's own ETag capture does) therefore fires SECOND.
    function fakeAjaxSync (method, model, options) {
      const doneCallbacks = []
      if (options.success) {
        doneCallbacks.push((data, status, jqXHR) => options.success(data, status, jqXHR))
      }
      return {
        done (cb) {
          doneCallbacks.push(cb)
          return this
        },
        resolve (data, status, jqXHR) {
          doneCallbacks.slice().forEach(cb => cb(data, status, jqXHR))
        }
      }
    }

    it('carries the revision captured on read through to the update sent by a cloned model', () => {
      const syncCalls = []
      spyOn(Backbone, 'sync').and.callFake((method, model, options) => {
        syncCalls.push({ method, options })
        return fakeAjaxSync(method, model, options)
      })

      const app = new PermissionApp()
      let clonedAtLoadTime
      // Mirrors PermissionAppView: `listenTo(this.model, 'loaded', this.render)`,
      // where render() immediately calls getPermission() to clone the model.
      app.listenTo(app, 'loaded', () => {
        clonedAtLoadTime = app.getPermission()
      })

      const xhr = app.loadPermission('doc1')
      const jqXHR = {
        getResponseHeader: (name) => (name === 'ETag' ? '"rev2"' : null)
      }
      xhr.resolve({ id: 'doc1', doctype: 'documents' }, 'success', jqXHR)

      expect(clonedAtLoadTime).toBeDefined()
      clonedAtLoadTime.save()

      const updateCall = syncCalls.find(call => call.method === 'update')
      expect(updateCall).toBeDefined()
      expect(updateCall.options.headers['If-Match']).toBe('rev2')
    })
  })
})
