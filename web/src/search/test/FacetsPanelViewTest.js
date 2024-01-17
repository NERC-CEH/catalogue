import { FacetsPanelView } from '../src/views'
import SearchApp from '../src/SearchApp'

describe('Test FacetsPanelView', () => {
    let view = null

    beforeEach(() => {
        const model = new SearchApp({ title: 'some text' })
        view = new FacetsPanelView({
            model
        })
    })

    it('View should be defined', () => {
        // then
        expect(view).toBeDefined()
    })
})
