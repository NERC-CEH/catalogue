import { EditorMetadata } from '../src'
import { CheckboxView } from '../src/views'

describe('Test CheckboxView', function () {
    let model = null
    let view = null

    beforeEach(function () {
        model = new EditorMetadata({ title: 'some text' })
        view = new CheckboxView({
            model,
            modelAttribute: 'notGEMINI',
            label: 'Exclude from GEMINI obligations',
            helpText: 'Some help text'
        })
    })

    it('when view is constructing should exist', () => {
        // then
        expect(view).toBeDefined()
    })

    it('renders', () => {
        view.render()
        expect(view.$('input')).toBeDefined()
    })
})
