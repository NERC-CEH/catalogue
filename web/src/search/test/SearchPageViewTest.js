import { SearchPageView } from '../src/views'
import SearchApp from '../src/SearchApp'

describe('Test SearchPageView', () => {
  let view = null

  beforeEach(() => {
    const model = new SearchApp({ title: 'some text' })
    view = new SearchPageView({
      model
    })
  })

  it('View should be defined', () => {
    // then
    expect(view).toBeDefined()
  })
})
