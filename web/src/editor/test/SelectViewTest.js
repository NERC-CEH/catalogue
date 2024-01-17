import { EditorMetadata, SingleView } from '../src'

describe('Test SelectView', () => {
    let view = null

    beforeEach(() => {
        const model = new EditorMetadata({ title: 'some text' })
        view = new SingleView({
            model
        })
    })

    it('View should be defined', () => {
        // then
        expect(view).toBeDefined()
    })

    it('test render', () => {
        // when
        view.render()
        // then
        expect(view.$('select')).toBeDefined()
    })
})
