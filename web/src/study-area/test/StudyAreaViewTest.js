import $ from 'jquery'
import { StudyAreaView } from '../src/View'

describe('Test StudyAreaView', function () {
  let el

  afterEach(() => el.remove())

  it('renders point', () => {
    // Given
    const templatePoint = '<div id="studyarea-map"><span content="{&quot;type&quot;:&quot;Feature&quot;,&quot;properties&quot;:{},&quot;geometry&quot;:{&quot;type&quot;:&quot;Point&quot;,&quot;coordinates&quot;:[-2.64501,54.5261]}}" dataType="geoJson"/></div>'

    // When
    el = $(templatePoint).appendTo($('body'))
    const view = new StudyAreaView({
      el
    })

    // Then
    expect(view).toBeDefined()
    expect(el.hasClass('leaflet-container')).toBeTrue()
  })

  it('renders polygon', () => {
    // Given
    const templatePolygon = '<div id="studyarea-map"><span content="{&quot;type&quot;:&quot;Feature&quot;,&quot;properties&quot;:{},&quot;geometry&quot;:{&quot;type&quot;:&quot;Polygon&quot;,&quot;coordinates&quot;:[[[-2.64866,54.52705],[-2.64363,54.52698],[-2.64406,54.52419],[-2.64872,54.52449],[-2.64866,54.52705]]]}}" dataType="geoJson"/></div>'

    // When
    el = $(templatePolygon).appendTo($('body'))
    const view = new StudyAreaView({
      el
    })

    // Then
    expect(view).toBeDefined()
    expect(el.hasClass('leaflet-container')).toBeTrue()
  })

  it('renders feature collection', () => {
    // Given
    const templateFeatureCollection = '<div id="studyarea-map"><span content="{&quot;type&quot;:&quot;FeatureCollection&quot;,&quot;features&quot;:[{&quot;type&quot;:&quot;Feature&quot;,&quot;properties&quot;:{},&quot;geometry&quot;:{&quot;type&quot;:&quot;Point&quot;,&quot;coordinates&quot;:[-2.64406,54.52601]}},{&quot;type&quot;:&quot;Feature&quot;,&quot;properties&quot;:{},&quot;geometry&quot;:{&quot;type&quot;:&quot;Point&quot;,&quot;coordinates&quot;:[-2.64501,54.5261]}}]}" dataType="geoJson"/></div>'

    // When
    el = $(templateFeatureCollection).appendTo($('body'))
    const view = new StudyAreaView({
      el
    })

    // Then
    expect(view).toBeDefined()
    expect(el.hasClass('leaflet-container')).toBeTrue()
  })
})
