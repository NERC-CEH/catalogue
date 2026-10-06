import { Geometry, GeometryView } from '../src/geometryMap'

describe('Test GeometryView', function () {
  let model = null
  let view = null

  const POINT = JSON.stringify({
    type: 'Feature',
    properties: {},
    geometry: { type: 'Point', coordinates: [1.71792, 52.65757] }
  })

  const POLYGON = JSON.stringify({
    type: 'Feature',
    properties: {},
    geometry: {
      type: 'Polygon',
      coordinates: [[[1.61, 52.61], [1.83, 52.61], [1.83, 52.69], [1.61, 52.69], [1.61, 52.61]]]
    }
  })

  const confidentialView = geometryString =>
    new GeometryView({
      model: new Geometry({ geometryString }),
      showConfidentialCheckbox: true
    })

  beforeEach(function () {
    model = new Geometry({ title: 'some text' })
    view = new GeometryView({ model })
  })

  it('renders the map container', () => {
    // when
    view.render()
    view.createMap()
    // then
    expect(view.$('.map').length).toBe(1)
  })

  it('constructs without a geometry', () => {
    expect(view.model.get('geometryString')).toBeUndefined()
  })

  // The checkbox is opt-in because GeometryView is also mounted by the
  // Infrastructure record editor, which has no confidentiality concept and no
  // template that honours the flag.
  it('hides the confidential checkbox unless it is asked for', () => {
    expect(view.$('#locationConfidential').length).toBe(0)
  })

  it('shows the confidential checkbox when it is enabled', () => {
    expect(confidentialView(POINT).$('#locationConfidential').length).toBe(1)
  })

  // Obfuscation is the server's job now. The editor records intent and nothing
  // else - it must not rewrite the geometry, because doing so in the browser is
  // what let the raw-JSON field and the circle tool publish precise locations.
  it('records the flag without touching a point geometry', () => {
    // given
    const confidential = confidentialView(POINT)
    spyOn(window, 'confirm').and.returnValue(true)

    // when
    confidential.$('#locationConfidential').prop('checked', true)
    confidential.handleLocationConfidentialCheckbox()

    // then
    expect(confidential.model.get('locationConfidential')).toBeTrue()
    expect(confidential.model.get('geometryString')).toBe(POINT)
  })

  it('records the flag without touching a polygon geometry', () => {
    // given
    const confidential = confidentialView(POLYGON)
    spyOn(window, 'confirm').and.returnValue(true)

    // when
    confidential.$('#locationConfidential').prop('checked', true)
    confidential.handleLocationConfidentialCheckbox()

    // then
    expect(confidential.model.get('locationConfidential')).toBeTrue()
    expect(confidential.model.get('geometryString')).toBe(POLYGON)
  })

  // The warning applies to every geometry type, not just points, because the
  // server reduces every type to a cell.
  it('warns before enabling confidentiality on a polygon', () => {
    // given
    const confidential = confidentialView(POLYGON)
    spyOn(window, 'confirm').and.returnValue(true)

    // when
    confidential.$('#locationConfidential').prop('checked', true)
    confidential.handleLocationConfidentialCheckbox()

    // then
    expect(window.confirm).toHaveBeenCalled()
  })

  it('does not enable confidentiality if the confirmation is cancelled', () => {
    // given
    const confidential = confidentialView(POINT)
    spyOn(window, 'confirm').and.returnValue(false)

    // when
    confidential.$('#locationConfidential').prop('checked', true)
    confidential.handleLocationConfidentialCheckbox()

    // then
    expect(confidential.model.get('locationConfidential')).not.toBeTrue()
    expect(confidential.$('#locationConfidential').is(':checked')).toBeFalse()
  })

  it('clears the flag without confirmation when unticked', () => {
    // given
    const confidential = confidentialView(POINT)
    confidential.model.set('locationConfidential', true)
    spyOn(window, 'confirm')

    // when
    confidential.$('#locationConfidential').prop('checked', false)
    confidential.handleLocationConfidentialCheckbox()

    // then
    expect(window.confirm).not.toHaveBeenCalled()
    expect(confidential.model.get('locationConfidential')).toBeFalse()
  })

  it('restores the ticked state from a saved record', () => {
    // given, when
    const saved = new GeometryView({
      model: new Geometry({ geometryString: POINT, locationConfidential: true }),
      showConfidentialCheckbox: true
    })

    // then
    expect(saved.$('#locationConfidential').is(':checked')).toBeTrue()
  })
})
