import Catalogue from '../src/CatalogueApp/Catalogue'
import CatalogueViewModel from '../src/CatalogueApp/CatalogueViewModel'
import Backbone from 'backbone'

describe('Catalogue model', () => {
  it('builds its url from the document id', () => {
    const model = new Catalogue({ id: 'abc-123' })
    expect(model.url()).toBe('/documents/abc-123/catalogue')
    expect(model.urlRoot()).toBe('/documents/abc-123/catalogue')
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
      const model = new Catalogue({ id: 'abc-123', value: 'eidc' })
      model.setRevision('rev1')
      model.save()
      expect(syncArgs.method).toBe('update')
      expect(syncArgs.options.headers['If-Match']).toBe('rev1')
    })

    it('does not send If-Match on read', () => {
      const model = new Catalogue({ id: 'abc-123' })
      model.setRevision('rev1')
      model.fetch()
      const headers = syncArgs.options.headers || {}
      expect(headers['If-Match']).toBeUndefined()
    })

    it('does not send If-Match when no revision is set', () => {
      const model = new Catalogue({ id: 'abc-123', value: 'eidc' })
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
      const model = new Catalogue({ id: 'abc-123', value: 'eidc' })
      model.setRevision('rev1')
      model.save()

      const jqXHR = {
        getResponseHeader: (name) => (name === 'ETag' ? '"rev2"' : null)
      }
      doneCallback({}, 'success', jqXHR)

      expect(model.getRevision()).toBe('rev2')
    })
  })
})

describe('CatalogueViewModel', () => {
  it('builds its url from the document id', () => {
    const model = new CatalogueViewModel({ id: 'abc-123' })
    expect(model.url()).toBe('/documents/abc-123/catalogue-view')
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
      const model = new CatalogueViewModel({ id: 'abc-123', value: [] })
      model.setRevision('rev1')
      model.save()
      expect(syncArgs.method).toBe('update')
      expect(syncArgs.options.headers['If-Match']).toBe('rev1')
    })

    it('does not send If-Match on read', () => {
      const model = new CatalogueViewModel({ id: 'abc-123' })
      model.setRevision('rev1')
      model.fetch()
      const headers = syncArgs.options.headers || {}
      expect(headers['If-Match']).toBeUndefined()
    })

    it('does not send If-Match when no revision is set', () => {
      const model = new CatalogueViewModel({ id: 'abc-123', value: [] })
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
      const model = new CatalogueViewModel({ id: 'abc-123', value: [] })
      model.setRevision('rev1')
      model.save()

      const jqXHR = {
        getResponseHeader: (name) => (name === 'ETag' ? '"rev2"' : null)
      }
      doneCallback({}, 'success', jqXHR)

      expect(model.getRevision()).toBe('rev2')
    })
  })
})
