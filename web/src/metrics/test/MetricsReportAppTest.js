import MetricsReportApp from '../src/MetricsReportApp'

describe('MetricsReportApp', function () {
  describe('getState', function () {
    it('keeps only truthy search fields', function () {
      // Attributes passed to the constructor are set before initialize binds
      // its change listeners, so this does not trigger a fetch.
      const model = new MetricsReportApp({
        startDate: '2020-01-01',
        endDate: '', // falsy -> dropped
        recordType: 'dataset',
        notASearchField: 'ignored'
      })

      expect(model.getState()).toEqual({
        startDate: '2020-01-01',
        recordType: 'dataset'
      })
    })

    it('excludes the metricsReport results array', function () {
      const model = new MetricsReportApp()
      expect(model.getState()).toEqual({})
    })
  })

  describe('setState', function () {
    it('applies the given attributes', function () {
      const model = new MetricsReportApp()
      spyOn(model, 'fetch')

      model.setState({ recordType: 'service' })

      expect(model.get('recordType')).toBe('service')
    })
  })

  describe('search wiring', function () {
    it('fetches with the current state when a search field changes', function () {
      const model = new MetricsReportApp()
      spyOn(model, 'fetch')

      model.set('startDate', '2021-05-05')

      expect(model.fetch).toHaveBeenCalled()
      const options = model.fetch.calls.mostRecent().args[0]
      expect(options.data).toEqual({ startDate: '2021-05-05' })
    })

    it('urls to the current pathname', function () {
      const model = new MetricsReportApp()
      expect(model.url()).toBe(window.location.pathname)
    })
  })
})
