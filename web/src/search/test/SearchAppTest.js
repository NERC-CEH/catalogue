import SearchApp from '../src/SearchApp'
import SearchPage from '../src/models/SearchPage'

describe('Test SearchApp getState serialization', () => {
  let model = null

  beforeEach(() => {
    model = new SearchApp()
  })

  it('omits op and page from a fresh (non-spatial, first-page) search', () => {
    const state = model.getState()

    expect(state.op).toBeUndefined()
    expect(state.page).toBeUndefined()
  })

  it('includes op once a bbox is set', () => {
    model.setBbox('-3.5,1.8,53,50')
    const state = model.getState()

    expect(state.bbox).toEqual('-3.5,1.8,53,50')
    expect(state.op).toEqual('intersects')
  })

  it('drops op again after the bbox is cleared', () => {
    model.setBbox('-3.5,1.8,53,50')
    model.clearBbox()
    const state = model.getState()

    expect(state.bbox).toBeUndefined()
    expect(state.op).toBeUndefined()
  })

  it('includes page only when it differs from the default', () => {
    model.set('page', 2)
    expect(model.getState().page).toEqual(2)

    model.set('page', model.defaults.page)
    expect(model.getState().page).toBeUndefined()
  })
})

/*
 * A semantic search embeds the term through Amazon Bedrock, so it is only meaningful
 * once a term has been committed. SearchFormView deliberately withholds the term while
 * the user types and commits it on submit -- but ticking the semantic checkbox writes
 * `semantic` straight to the model, and `semantic` is itself a search field, so the
 * toggle started a search of its own. With an empty box that produced
 * `GET /eidc/documents?semantic=true` with no term, which the server defaults to "*";
 * KNN then embeds the literal string "*" and returns whatever sits nearest that
 * arbitrary point. One wasted Bedrock call per toggle, and a flash of junk results.
 */
describe('SearchApp semantic search guard', () => {
  let model = null

  beforeEach(() => {
    model = new SearchApp()
    spyOn(SearchPage.prototype, 'fetch')
  })

  it('does not search when semantic mode is switched on with no term', () => {
    model.set({ semantic: true, term: '' }, { silent: true })

    model.performSearch({ changed: { semantic: true, term: '' } })

    expect(SearchPage.prototype.fetch).not.toHaveBeenCalled()
  })

  it('does not search a semantic query whose term has been cleared', () => {
    model.set({ semantic: true, term: undefined }, { silent: true })

    model.performSearch({ changed: { semantic: true } })

    expect(SearchPage.prototype.fetch).not.toHaveBeenCalled()
  })

  it('searches once a semantic query has a term, as the search button commits one', () => {
    model.set({ semantic: true, term: 'nitrogen deposition' }, { silent: true })

    model.performSearch({ changed: { term: 'nitrogen deposition' } })

    expect(SearchPage.prototype.fetch).toHaveBeenCalled()
  })

  it('still searches a keyword query with no term, where "*" is meaningful', () => {
    model.set({ semantic: false, term: '' }, { silent: true })

    model.performSearch({ changed: { term: '' } })

    expect(SearchPage.prototype.fetch).toHaveBeenCalled()
  })
})
