import $ from 'jquery'
import MetricsReportApp from '../src/MetricsReportApp'
import MetricsReportForm from '../src/MetricsReportForm'

describe('MetricsReportForm', () => {
  let model
  let view

  beforeEach(() => {
    $(document.body).html(`
      <form id='mf'>
        <input name='startDate' value='2020-01-01'>
        <input name='notASearchField' value='ignored'>
        <input type='checkbox' name='recordType' value='dataset' checked>
        <input type='checkbox' name='recordType' value='service'>
      </form>`)
    model = new MetricsReportApp()
    spyOn(model, 'fetch') // onSubmit sets fields -> change -> doSearch -> fetch
    view = new MetricsReportForm({ el: '#mf', model })
  })

  afterEach(() => {
    $(document.body).empty()
  })

  it('prevents the default submit', () => {
    const event = jasmine.createSpyObj('event', ['preventDefault'])
    view.onSubmit(event)
    expect(event.preventDefault).toHaveBeenCalled()
  })

  it('copies recognised search fields from the form onto the model', () => {
    view.onSubmit(jasmine.createSpyObj('event', ['preventDefault']))
    expect(model.get('startDate')).toBe('2020-01-01')
  })

  it('collects only the checked recordType values', () => {
    view.onSubmit(jasmine.createSpyObj('event', ['preventDefault']))
    expect(model.get('recordType')).toEqual(['dataset'])
  })

  it('ignores form inputs that are not search fields', () => {
    view.onSubmit(jasmine.createSpyObj('event', ['preventDefault']))
    expect(model.get('notASearchField')).toBeUndefined()
  })
})
