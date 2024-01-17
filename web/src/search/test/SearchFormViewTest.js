import { SearchFormView } from '../src/views'
import SearchApp from '../src/SearchApp'

describe('Test SearchFormView', () => {
    let view = null

    beforeEach(() => {
        const model = new SearchApp({ title: 'some text' })
        view = new SearchFormView({
            model
        })
    })

    it('View should be defined', () => {
        // then
        expect(view).toBeDefined()
    })
})
