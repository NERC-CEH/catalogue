import Backbone from 'backbone'
import SearchRouter from '../src/SearchRouter'

describe('SearchRouter', function () {
  // A minimal model stand-in: SearchRouter only needs getState/setState plus
  // Backbone's event mixin. Using the real SearchApp would trigger its
  // debounced performSearch (an AJAX call) on every change.
  function fakeModel (state = {}) {
    const model = new Backbone.Model()
    model.getState = () => state
    model.setState = jasmine.createSpy('setState')
    return model
  }

  function newRouter (state, locationOverrides = {}) {
    const location = {
      href: 'http://localhost/eidc/search',
      hash: '#',
      search: '',
      ...locationOverrides
    }
    return new SearchRouter({ model: fakeModel(state), location })
  }

  describe('initialize', function () {
    it('strips hash and query from the app url', function () {
      const router = newRouter({}, { href: 'http://localhost/eidc/search?term=a#foo' })
      expect(router.appUrl).toBe('http://localhost/eidc/search')
    })

    it('seeds the model from the query string when there is no hash', function () {
      const router = newRouter({}, { hash: '', search: '?term=init' })
      expect(router.model.setState).toHaveBeenCalledWith(
        jasmine.objectContaining({ term: 'init' }),
        { silent: true }
      )
    })

    it('does not seed the model when a hash is present', function () {
      const router = newRouter({}, { hash: '#term=a', search: '?term=a' })
      expect(router.model.setState).not.toHaveBeenCalled()
    })
  })

  describe('updateRoute', function () {
    let replaceState

    beforeEach(function () {
      replaceState = spyOn(window.history, 'replaceState')
    })

    function urlFor (state) {
      const router = newRouter(state)
      router.updateRoute()
      return replaceState.calls.mostRecent().args[2]
    }

    it('serialises scalar fields, url-encoding values', function () {
      expect(urlFor({ term: 'water quality', page: 2 }))
        .toBe('http://localhost/eidc/search?term=water%20quality&page=2')
    })

    it('keeps a single facet value as field|value', function () {
      expect(urlFor({ facet: ['resourceType|dataset'] }))
        .toBe('http://localhost/eidc/search?facet=resourceType%7Cdataset')
    })

    it('groups multiple values of one facet field into field|(a OR b)', function () {
      expect(urlFor({ facet: ['resourceType|dataset', 'resourceType|service'] }))
        .toBe('http://localhost/eidc/search?facet=resourceType%7C(dataset%20OR%20service)')
    })

    it('emits a separate facet param per distinct field', function () {
      expect(urlFor({ facet: ['resourceType|dataset', 'topic|climate'] }))
        .toBe('http://localhost/eidc/search?facet=resourceType%7Cdataset&facet=topic%7Cclimate')
    })

    it('produces a bare url when the state is empty', function () {
      expect(urlFor({})).toBe('http://localhost/eidc/search')
    })
  })

  describe('updateModel', function () {
    it('parses the query string and delegates to setState', function () {
      const router = newRouter({})
      router.model.setState.calls.reset()

      router.updateModel('term=foo&page=3')

      expect(router.model.setState).toHaveBeenCalledWith(
        jasmine.objectContaining({ term: 'foo' }),
        undefined
      )
    })

    it('does nothing when given an empty state', function () {
      const router = newRouter({})
      router.model.setState.calls.reset()

      router.updateModel('')
      router.updateModel(undefined)

      expect(router.model.setState).not.toHaveBeenCalled()
    })
  })
})
