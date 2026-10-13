import $ from 'jquery'
import { SearchPageView } from '../src/views'
import SearchApp from '../src/SearchApp'

describe('Test SearchPageView', () => {
  let view = null
  let model = null

  beforeEach(() => {
    model = new SearchApp({ title: 'some text' })
    view = new SearchPageView({
      model
    })
  })

  it('View should be defined', () => {
    expect(view).toBeDefined()
  })

  describe('handleSortChange', () => {
    it('splits a "field-order" value onto the model', () => {
      view.handleSortChange({ currentTarget: { value: 'title-desc' } })

      expect(model.get('sortField')).toBe('title')
      expect(model.get('order')).toBe('desc')
    })

    it('sets a field with no order suffix and leaves order null', () => {
      view.handleSortChange({ currentTarget: { value: 'title' } })

      expect(model.get('sortField')).toBe('title')
      expect(model.get('order')).toBeNull()
    })

    it('clears both fields for an empty value', () => {
      view.handleSortChange({ currentTarget: { value: '' } })

      expect(model.get('sortField')).toBeNull()
      expect(model.get('order')).toBeNull()
    })
  })

  describe('with a page of results already in the DOM', () => {
    beforeEach(() => {
      $(document.body).html(`
        <div id='page'>
          <input id='num-records' value='42'>
          <div class='result' id='rec1' data-location='1,2,3,4'>
            <span class='result__title'>Title One</span>
            <span class='result__description'>Description one</span>
          </div>
          <div class='result' id='rec2' data-location='5,6,7,8'>
            <span class='result__title'>Title Two</span>
            <span class='result__description'>Description two</span>
          </div>
        </div>`)
      model = new SearchApp({})
      view = new SearchPageView({ el: '#page', model })
    })

    afterEach(() => {
      $(document.body).empty()
    })

    it('hydrates the results model from the rendered HTML', () => {
      const results = model.getResults()
      expect(results.get('numFound')).toBe('42')
      expect(results.get('results').length).toEqual(2)
      expect(results.get('results')[0].identifier).toBe('rec1')
      expect(results.get('results')[0].title).toBe('Title One')
    })

    it('highlights only the selected result', () => {
      model.getResults().set('selected', 'rec2')

      view.updateSelected()

      expect($('#rec1').hasClass('selected')).toBe(false)
      expect($('#rec2').hasClass('selected')).toBe(true)
    })

    it('empties its element on clear', () => {
      view.clear()
      expect(view.$el.children().length).toEqual(0)
    })
  })

  describe('rendering search results (dri-one #454)', () => {
    const payload = 'Soil "X" <img src=x onerror="window.__xss454=1">'

    beforeEach(() => {
      $(document.body).html("<div id='page'></div>")
      model = new SearchApp({})
      view = new SearchPageView({ el: '#page', model })
    })

    afterEach(() => {
      $(document.body).empty()
    })

    it('shows record titles, descriptions and related searches as text', () => {
      model.getResults().set({
        numFound: 1,
        page: 1,
        rows: 20,
        url: '/eidc/documents',
        results: [{
          identifier: 'rec1',
          title: payload,
          shortenedDescription: payload,
          documentType: 'GEMINI_DOCUMENT',
          recordType: 'Dataset',
          state: 'published',
          url: '/documents/rec1'
        }],
        relatedSearches: [{ href: '/eidc/documents?term="soil"&x=1', title: payload }]
      })

      view.render()

      expect(view.$('img').length).toBe(0)
      expect(view.$('.result__title').first().text()).toBe(payload)
      const related = view.$('.results__related_searches a')
      expect(related.text()).toBe(payload)
      expect(related.attr('href')).toBe('/eidc/documents?term="soil"&x=1')
    })
  })
})
