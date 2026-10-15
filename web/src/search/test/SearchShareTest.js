import $ from 'jquery'
import Backbone from 'backbone'
import { MAILTO_LIMIT, shareEmail } from '../src/views/SearchPageView'
import { SearchPageView } from '../src/views'

// "Email search results": the catalogue and the day in the subject, the search's address, and each
// result on the page by title and address
describe('Emailing a search', () => {
  const date = new Date(2026, 9, 15)
  const href = 'https://catalogue.ceh.ac.uk/eidc/documents?term=rain&page=2'
  const result = (identifier, title) => ({ identifier, title })
  const read = link => {
    const url = new URL(link)
    return { subject: url.searchParams.get('subject'), body: url.searchParams.get('body') }
  }

  it('names the catalogue and the day in the subject', () => {
    expect(read(shareEmail({ catalogue: 'EIDC', href, date })).subject).toBe('EIDC search results, 15 October 2026')
  })

  it('gives the search\'s address, then each result by title and address, in order', () => {
    const link = shareEmail({ catalogue: 'EIDC', href, date, results: [result('abc', 'Rainfall 1960-2020'), result('def', 'Rain gauges')] })

    expect(link.startsWith('mailto:?subject=')).toBeTrue()
    expect(read(link).body).toBe([
      'Search: https://catalogue.ceh.ac.uk/eidc/documents?term=rain',
      '1. Rainfall 1960-2020\r\n   https://catalogue.ceh.ac.uk/documents/abc',
      '2. Rain gauges\r\n   https://catalogue.ceh.ac.uk/documents/def'
    ].join('\r\n\r\n'))
  })

  it('tidies a title read from the page', () => {
    const { body } = read(shareEmail({ catalogue: 'EIDC', href, date, results: [result('abc', '\n   Rainfall\n   1960-2020  ')] }))

    expect(body).toContain('1. Rainfall 1960-2020\r\n')
  })

  it('gives just the search\'s address when there are no results', () => {
    expect(read(shareEmail({ catalogue: 'EIDC', href, date })).body).toBe('Search: https://catalogue.ceh.ac.uk/eidc/documents?term=rain')
  })

  it('stays short enough for mail programs, counting the results it leaves out', () => {
    const results = Array.from({ length: 20 }, (_, i) => result(`record-${i}`, `A long title about rainfall, rivers and soils, number ${i + 1}`))

    const link = shareEmail({ catalogue: 'EIDC', href, date, results })
    const { body } = read(link)

    expect(link.length).toBeLessThanOrEqual(MAILTO_LIMIT)
    const listed = body.split('\r\n\r\n').filter(entry => /^\d+\. /.test(entry))
    expect(listed.length).toBeGreaterThan(0)
    expect(listed[0]).toContain('number 1\r\n')
    expect(body).toMatch(new RegExp(`…and ${20 - listed.length} more: see the search link$`))
  })

  it('leaves out even the first result if it alone is too long', () => {
    const { body } = read(shareEmail({ catalogue: 'EIDC', href, date, limit: 300, results: [result('abc', 'x'.repeat(400))] }))

    expect(body).toBe('Search: https://catalogue.ceh.ac.uk/eidc/documents?term=rain\r\n\r\n…and 1 more: see the search link')
  })

  describe('from the results page', () => {
    let view, opened

    beforeEach(() => {
      $('head').append('<meta name="og.site_name" content="Environmental Information Data Centre">')
      $('html').attr('data-catalogue', 'eidc')
      const model = new Backbone.Model()
      const page = new Backbone.Model()
      model.getResults = () => page
      view = new SearchPageView({ model, el: $('<div class="results">').appendTo(document.body) })
      // As a search's answer arrives, after the view has read the (empty) page
      page.set({ numFound: 2, page: 1, rows: 20, url: href, results: [result('abc', 'Rainfall 1960-2020'), result('def', 'Rain gauges')] })
      opened = spyOn(view, 'openMail')
      view.render()
    })

    afterEach(() => {
      $('meta[name="og.site_name"]').remove()
      $('html').removeAttr('data-catalogue').removeData('catalogue')
      $(document.body).empty()
    })

    it('emails the results shown, under the catalogue\'s name', () => {
      view.$('#searchShareIcon').trigger('click')

      const { subject, body } = read(opened.calls.mostRecent().args[0])
      expect(subject).toMatch(/^Environmental Information Data Centre search results, \d{1,2} \w+ \d{4}$/)
      expect(body).toContain('1. Rainfall 1960-2020\r\n')
      expect(body).toContain('2. Rain gauges\r\n')
    })

    it('falls back to the catalogue\'s id when the page doesn\'t name it', () => {
      $('meta[name="og.site_name"]').remove()

      view.$('#searchShareIcon').trigger('click')

      expect(read(opened.calls.mostRecent().args[0]).subject).toMatch(/^EIDC search results, /)
    })
  })
})
