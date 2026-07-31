import { EditorMetadata } from '../src'
import { ServiceAgreement } from '../src/models'
import Backbone from 'backbone'

describe('EditorMetadata', () => {
  describe('validation', () => {
    it('success with title', () => {
      const editor = new EditorMetadata({
        title: 'this is a title'
      })
      const errors = editor.validate(editor.attributes)
      expect(errors).not.toBeDefined()
    })

    it('produces error without title', () => {
      const editor = new EditorMetadata()
      const errors = editor.validate(editor.attributes)
      expect(errors).toHaveSize(1)
    })
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
      const model = new EditorMetadata({ id: 'doc1', title: 'this is a title' })
      model.setRevision('rev1')
      model.set('id', 'doc1')
      model.save()
      expect(syncArgs.method).toBe('update')
      expect(syncArgs.options.headers['If-Match']).toBe('rev1')
    })

    it('does not send If-Match when no revision is set (create)', () => {
      const model = new EditorMetadata({ title: 'this is a title' })
      model.save()
      const headers = syncArgs.options.headers || {}
      expect(headers['If-Match']).toBeUndefined()
    })
  })

  describe('revision refresh from ETag', () => {
    let doneCallback

    beforeEach(() => {
      spyOn(Backbone, 'sync').and.callFake(() => {
        return {
          done: (cb) => { doneCallback = cb }
        }
      })
    })

    it('stores the de-quoted response ETag as the new revision after save', () => {
      const model = new EditorMetadata({ id: 'doc1', title: 'this is a title' })
      model.setRevision('rev1')
      model.save()

      const jqXHR = {
        getResponseHeader: (name) => (name === 'ETag' ? '"rev2"' : null)
      }
      doneCallback({}, 'success', jqXHR)

      expect(model.getRevision()).toBe('rev2')
    })
  })

  // ServiceAgreement calls EditorMetadata.prototype.initialize with no arguments, so it is worth
  // pinning that it still picks up the revision plumbing - the service-agreement editor is a long-lived
  // depositor session and relies entirely on inheriting this.
  describe('ServiceAgreement inherits the optimistic locking behaviour', () => {
    let syncArgs

    beforeEach(() => {
      spyOn(Backbone, 'sync').and.callFake((method, model, options) => {
        syncArgs = { method, options }
        return { done: () => {} }
      })
    })

    it('sends If-Match on update when a revision is set', () => {
      const model = new ServiceAgreement({ id: 'sa1', title: 'this is a title', depositorContactDetails: 'a@b.com' }, { id: 'sa1' })
      model.setRevision('metaRev1:rawRev1')
      model.save()
      expect(syncArgs.method).toBe('update')
      expect(syncArgs.options.headers['If-Match']).toBe('metaRev1:rawRev1')
    })

    it('does not send If-Match before a revision has been captured', () => {
      const model = new ServiceAgreement({ id: 'sa1', title: 'this is a title', depositorContactDetails: 'a@b.com' }, { id: 'sa1' })
      model.save()
      const headers = syncArgs.options.headers || {}
      expect(headers['If-Match']).toBeUndefined()
    })
  })
})
