import SearchApp from '../src/SearchApp'

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
