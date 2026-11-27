import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import $ from 'jquery'
import MetricsReportApp from '../src/MetricsReportApp'
import MetricsReportForm, { DOC_ID_ERROR } from '../src/MetricsReportForm'

describe('MetricsReportForm', () => {
  let model
  let view

  beforeEach(() => {
    $(document.body).html(`
      <form id='mf'>
        <input name='startDate' value='2020-01-01'>
        <input name='docId' value=''>
        <input name='notASearchField' value='ignored'>
        <input type='checkbox' name='recordType' value='dataset' checked>
        <input type='checkbox' name='recordType' value='service'>
      </form>`)
    model = new MetricsReportApp()
    vi.spyOn(model, 'fetch').mockImplementation(() => {}) // onSubmit sets fields -> change -> doSearch -> fetch
    view = new MetricsReportForm({ el: '#mf', model })
  })

  afterEach(() => {
    $(document.body).empty()
  })

  it('prevents the default submit', () => {
    const event = { preventDefault: vi.fn() }
    view.onSubmit(event)
    expect(event.preventDefault).toHaveBeenCalled()
  })

  it('copies recognised search fields from the form onto the model', () => {
    view.onSubmit({ preventDefault: vi.fn() })
    expect(model.get('startDate')).toBe('2020-01-01')
  })

  it('collects only the checked recordType values', () => {
    view.onSubmit({ preventDefault: vi.fn() })
    expect(model.get('recordType')).toEqual(['dataset'])
  })

  it('ignores form inputs that are not search fields', () => {
    view.onSubmit({ preventDefault: vi.fn() })
    expect(model.get('notASearchField')).toBeUndefined()
  })

  describe('document ID validation', () => {
    const VALID_ID = 'abcd1234-abcd-1234-abcd-1234abcd1234'
    const docIdInput = () => view.$('input[name=docId]')
    const feedback = () => docIdInput().siblings('.invalid-feedback')

    it.each([
      'abcd1234-abcd-1234-abcd-1234abcd1234',
      'abcd1234-abcd-1234-abcd-1234abcd1235',
      'abcd1234-abcd-1234-abcd-1234abcd1236'
    ])('submits the well-formed document ID %s', (goodId) => {
      docIdInput().val(goodId)
      view.onSubmit({ preventDefault: vi.fn() })
      expect(model.get('docId')).toBe(goodId)
      expect(model.fetch).toHaveBeenCalledTimes(1)
      expect(docIdInput().hasClass('is-invalid')).toBe(false)
    })

    it('accepts upper case and trims surrounding spaces', () => {
      docIdInput().val(`  ${VALID_ID.toUpperCase()} `)
      view.onSubmit({ preventDefault: vi.fn() })
      expect(model.get('docId')).toBe(VALID_ID.toUpperCase())
    })

    it('submits when the document ID is left empty', () => {
      view.onSubmit({ preventDefault: vi.fn() })
      expect(model.fetch).toHaveBeenCalledTimes(1)
      expect(docIdInput().hasClass('is-invalid')).toBe(false)
    })

    it.each([
      'abc', // too short
      'abcd1234abcd1234abcd1234abcd1234', // no hyphens
      'abcd1234-abcd-1234-abcd-1234abcd123', // last group too short
      'abcd1234-abcd-1234-abcd-1234abcd12345', // last group too long
      'abcd1234-abcd-1234-abcd-1234abcd123z', // non-hex character
      'abcd1234_abcd_1234_abcd_1234abcd1234' // wrong separator
    ])('stops the submit and shows an error for %s', (badId) => {
      const event = { preventDefault: vi.fn() }
      docIdInput().val(badId)
      view.onSubmit(event)
      expect(event.preventDefault).toHaveBeenCalled()
      expect(model.fetch).not.toHaveBeenCalled()
      expect(model.get('docId')).toBeUndefined()
      expect(model.get('startDate')).toBeUndefined()
      expect(docIdInput().hasClass('is-invalid')).toBe(true)
      expect(docIdInput().attr('aria-invalid')).toBe('true')
      expect(feedback().text()).toBe(DOC_ID_ERROR)
    })

    it('clears the error once the ID is corrected and submitted', () => {
      docIdInput().val('abc')
      view.onSubmit({ preventDefault: vi.fn() })
      docIdInput().val(VALID_ID)
      view.onSubmit({ preventDefault: vi.fn() })
      expect(docIdInput().hasClass('is-invalid')).toBe(false)
      expect(feedback().text()).toBe('')
      expect(model.get('docId')).toBe(VALID_ID)
    })

    it('clears the error as soon as the user edits the ID', () => {
      docIdInput().val('abc')
      view.onSubmit({ preventDefault: vi.fn() })
      docIdInput().val('abcd').trigger('input')
      expect(docIdInput().hasClass('is-invalid')).toBe(false)
    })
  })
})
