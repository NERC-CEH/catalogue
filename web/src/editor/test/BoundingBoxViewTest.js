import { BoundingBoxView, BoundingBox } from '../src/geometryMap'
import template from '../src/geometryMap/geometryTemplate'

describe('Test BoundingBoxView', function () {
    let model = null
    let view = null

    beforeEach(function () {
        model = new BoundingBox({ title: 'some text', template })
        view = new BoundingBoxView({ model })
    })

    it('test map is rendered', () => {
        // when
        view.render()
        // then
        expect(view.$('.map').length).toEqual(1) // "view.$('.map')).toBeDefined()" is not used here since it returns true regardless of the jquery selector used
    })

    it('when view is constructing should exist', () => {
        // then
        expect(view).toBeDefined()
    })
})
