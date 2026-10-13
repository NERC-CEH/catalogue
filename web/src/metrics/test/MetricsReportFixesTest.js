import $ from 'jquery'
import MetricsReportApp from '../src/MetricsReportApp'
import MetricsReportForm from '../src/MetricsReportForm'
import MetricsReportResults from '../src/MetricsReportResults'

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
    spyOn(app, 'fetch')
    const form = new MetricsReportForm({ el: '#mf', model: app })
    form.onSubmit(jasmine.createSpyObj('event', ['preventDefault']))
    expect(app.getState().docId).toBe('abc')

    $('input[name=docId]').val('')
    form.onSubmit(jasmine.createSpyObj('event', ['preventDefault']))

    expect(app.getState().docId).toBeUndefined()
    expect(app.fetch.calls.mostRecent().args[0].data.docId).toBeUndefined()
  })

  it('makes one request for one submit, however many fields changed', () => {
    spyOn(app, 'fetch')
    const form = new MetricsReportForm({ el: '#mf', model: app })

    form.onSubmit(jasmine.createSpyObj('event', ['preventDefault']))

    expect(app.fetch).toHaveBeenCalledTimes(1)
  })

  it('replaces the old rows with the reason when the report fails', () => {
    jasmine.Ajax.install()
    try {
      const results = new MetricsReportResults({ el: '#mt', model: app })
      app.set('docId', 'abc')
      jasmine.Ajax.requests.mostRecent().respondWith({ status: 400, responseText: 'Start date must be before end date' })

      expect(results.$('tbody').text()).not.toContain('Old record')
      expect(results.$('tbody').text()).toContain('The report could not be produced: Start date must be before end date')
    } finally {
      jasmine.Ajax.uninstall()
    }
  })
})
