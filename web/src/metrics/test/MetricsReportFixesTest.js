import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import $ from 'jquery'
import MetricsReportApp from '../src/MetricsReportApp'
import MetricsReportForm from '../src/MetricsReportForm'
import MetricsReportResults from '../src/MetricsReportResults'
import { fakeServer } from '../../test/support'

/**
 * A metrics filter could not be cleared, one submit fetched once per changed field, and a failed
 * report left the previous table on screen with no message (NERC-CEH/dri-one#461).
 */
describe('Metrics report', () => {
  let app

  beforeEach(() => {
    $(document.body).html(`
      <form id="mf">
        <input name="startDate" value="2026-10-01">
        <input name="endDate" value="2026-10-13">
        <input name="docId" value="abc">
      </form>
      <table id="mt"><tbody><tr><td>old</td><td>Old record</td><td>dataset</td><td>1</td><td>2</td></tr></tbody></table>`)
    app = new MetricsReportApp()
  })

  afterEach(() => $(document.body).empty())

  it('clears a filter emptied on the form', () => {
    vi.spyOn(app, 'fetch').mockImplementation(() => {})
    const form = new MetricsReportForm({ el: '#mf', model: app })
    form.onSubmit({ preventDefault: vi.fn() })
    expect(app.getState().docId).toBe('abc')

    $('input[name=docId]').val('')
    form.onSubmit({ preventDefault: vi.fn() })

    expect(app.getState().docId).toBeUndefined()
    expect(app.fetch.mock.lastCall[0].data.docId).toBeUndefined()
  })

  it('makes one request for one submit, however many fields changed', () => {
    vi.spyOn(app, 'fetch').mockImplementation(() => {})
    const form = new MetricsReportForm({ el: '#mf', model: app })

    form.onSubmit({ preventDefault: vi.fn() })

    expect(app.fetch).toHaveBeenCalledTimes(1)
  })

  it('shows each fetched row\'s document id', () => {
    fakeServer.install()
    try {
      const results = new MetricsReportResults({ el: '#mt', model: app })
      app.set('docId', 'abc')
      fakeServer.lastRequest().respondWith({
        status: 200,
        responseText: JSON.stringify({ metricsReport: [{ document: 'abc123', docTitle: 'Rainfall', recordType: 'dataset', views: '3', downloads: '1' }] })
      })

      expect(results.$('tbody td').map((i, td) => td.textContent).get()).toEqual(['abc123', 'Rainfall', 'dataset', '3', '1'])
    } finally {
      fakeServer.uninstall()
    }
  })

  it('replaces the old rows with the reason when the report fails', () => {
    fakeServer.install()
    try {
      const results = new MetricsReportResults({ el: '#mt', model: app })
      app.set('docId', 'abc')
      fakeServer.lastRequest().respondWith({ status: 400, responseText: 'Start date must be before end date' })

      expect(results.$('tbody').text()).not.toContain('Old record')
      expect(results.$('tbody').text()).toContain('The report could not be produced: Start date must be before end date')
    } finally {
      fakeServer.uninstall()
    }
  })

  describe('a report that finds nothing', () => {
    const answer = body => fakeServer.lastRequest().respondWith({ status: 200, responseText: JSON.stringify(body) })
    const cells = results => results.$('tbody td').map((i, td) => td.textContent).get()

    beforeEach(() => fakeServer.install())
    afterEach(() => fakeServer.uninstall())

    it('says no records match, in place of the old rows', () => {
      const results = new MetricsReportResults({ el: '#mt', model: app })
      app.set('docId', 'abcd0001-0000-4000-8000-000000000001')
      answer({ metricsReport: [] })
      expect(results.$('tbody').text()).not.toContain('Old record')
      expect(cells(results)).toEqual(['No records match these filters.'])
    })

    it('replaces an error even when the rows are the same as before it', () => {
      const results = new MetricsReportResults({ el: '#mt', model: app })
      app.set('docId', 'a')
      answer({ metricsReport: [] })
      app.set('docId', 'b')
      fakeServer.lastRequest().respondWith({ status: 500, responseText: 'boom' })
      expect(results.$('tbody').text()).toContain('could not be produced')
      app.set('docId', 'c')
      answer({ metricsReport: [] })
      expect(cells(results)).toEqual(['No records match these filters.'])
    })

    it('does not read a message row on the page as a record', () => {
      $('#mt tbody').html('<tr><td colspan="5">No records match these filters.</td></tr>')
      // eslint-disable-next-line no-new
      new MetricsReportResults({ el: '#mt', model: app })
      expect(app.get('metricsReport')).toEqual([])
    })
  })

  it('clears the old rows when the server leaves an empty report out', () => {
    fakeServer.install()
    try {
      const results = new MetricsReportResults({ el: '#mt', model: app })
      app.set('docId', 'abcd0001-0000-4000-8000-000000000001')
      // As the app's mapper sends it: non_empty drops the empty list
      fakeServer.lastRequest().respondWith({ status: 200, responseText: '{"catalogue":"eidc"}' })
      expect(app.get('metricsReport')).toEqual([])
      expect(results.$('tbody').text()).not.toContain('Old record')
      expect(results.$('tbody td').map((i, td) => td.textContent).get()).toEqual(['No records match these filters.'])
    } finally {
      fakeServer.uninstall()
    }
  })
})
