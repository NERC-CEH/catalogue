import { Geometry, GeometryView } from '../src/geometryMap'

describe('Test GeometryView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    model = new Geometry({ title: 'some text' })
    view = new GeometryView({ model })
  })

  it('test map is rendered', () => {
    // when
    view.render()
    view.createMap()
    // then
    expect(view.$('.map')).toBeDefined()
  })

  it('when view is constructing should exist', () => {
    // then
    setTimeout(expect(view).toBeDefined(), 5000)
  })

  it('converts a point into a square when confidential is enabled', () => {
    view.model.set('locationConfidential', true)
    view.model.set('geometryString', JSON.stringify({
      type: 'Point',
      coordinates: [-2.64501, 54.5261]
    }))

    view.handleLocationConfidentialChange()

    const geometry = JSON.parse(view.model.get('geometryString'))

    expect(geometry.type).toBe('Polygon')
  })

  it('does not modify geometry when confidentiality is enabled for a polygon', () => {
    const originalGeometry = JSON.stringify({
      type: 'Polygon',
      coordinates: [[[0, 0], [1, 0], [1, 1], [0, 1], [0, 0]]]
    })

    view.model.set('locationConfidential', true)
    view.model.set('geometryString', originalGeometry)

    view.handleLocationConfidentialChange()

    expect(view.model.get('geometryString')).toBe(originalGeometry)
  })

  it('shows a confirmation when enabling confidentiality for a point', () => {
    spyOn(window, 'confirm').and.returnValue(true)

    view.model.set('geometryString', JSON.stringify({
      type: 'Feature',
      geometry: {
        type: 'Point',
        coordinates: [-2.64501, 54.5261]
      }
    }))

    view.$('#locationConfidential').prop('checked', true)

    view.handleLocationConfidentialCheckbox()

    expect(window.confirm).toHaveBeenCalled()
    expect(view.model.get('locationConfidential')).toBeTrue()
  })

  it('does not enable confidentiality if confirmation is cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false)

    view.model.set('geometryString', JSON.stringify({
      type: 'Feature',
      geometry: {
        type: 'Point',
        coordinates: [-2.64501, 54.5261]
      }
    }))

    view.$('#locationConfidential').prop('checked', true)

    view.handleLocationConfidentialCheckbox()

    expect(window.confirm).toHaveBeenCalled()
    expect(view.model.get('locationConfidential')).not.toBeTrue()
    expect(view.$('#locationConfidential').is(':checked')).toBeFalse()
  })

  it('does not show confirmation when enabling confidentiality for a polygon', () => {
    spyOn(window, 'confirm')

    view.model.set('geometryString', JSON.stringify({
      type: 'Feature',
      geometry: {
        type: 'Polygon',
        coordinates: [[
          [0, 0],
          [1, 0],
          [1, 1],
          [0, 1],
          [0, 0]
        ]]
      }
    }))

    view.$('#locationConfidential').prop('checked', true)

    view.handleLocationConfidentialCheckbox()

    expect(window.confirm).not.toHaveBeenCalled()
    expect(view.model.get('locationConfidential')).toBeTrue()
  })

  it('does not show confirmation when enabling confidentiality if no geometry exists', () => {
    spyOn(window, 'confirm')

    view.model.set('geometryString', null)

    view.$('#locationConfidential').prop('checked', true)

    view.handleLocationConfidentialCheckbox()

    expect(window.confirm).not.toHaveBeenCalled()
    expect(view.model.get('locationConfidential')).toBeTrue()
  })
})
