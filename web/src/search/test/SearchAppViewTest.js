import SearchAppView from '../src/SearchAppView'
import SearchApp from '../src/SearchApp'

describe('Test SearchAppView', () => {
    let view = null

    beforeEach(() => {
        const model = new SearchApp({ title: 'some text' })
        view = new SearchAppView({
            model
        })
    })

    it('View should be defined', () => {
        // then
        expect(view).toBeDefined()
    })

    it('Sub views should be defined and use the same model', () => {
        // then
        expect(view.searchFormView).toBeDefined()
        expect(view.searchResultsView).toBeDefined()
        expect(view.facetsPanelView).toBeDefined()

        expect(view.searchFormView.model.get('title')).toEqual('some text')
        expect(view.searchResultsView.model.get('title')).toEqual('some text')
        expect(view.facetsPanelView.model.get('title')).toEqual('some text')
    })
})
