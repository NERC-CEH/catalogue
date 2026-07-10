import SpatialFilterView from '../src/views/SpatialFilterView'
import SearchApp from '../src/SearchApp'
import $ from 'jquery'

describe('Test SpatialFilterView', () => {
  const newView = attrs => {
    $(document.body).html('<div class=\'mapsearch\'></div>')
    return new SpatialFilterView({
      model: new SearchApp(attrs),
      el: $('.mapsearch')
    })
  }

  describe('setBbox rounds coordinates to three decimal places', () => {
    it('serialises west,east,north,south rounded to 3dp', () => {
      // given a Leaflet layer whose bounds carry floating-point noise
      const layer = {
        getBounds: () => ({
          getWest: () => -5.031738281250001,
          getEast: () => 0.28564453125000006,
          getNorth: () => 61.25999938187381,
          getSouth: () => 59.25500057188834
        })
      }
      const view = newView({})

      // when
      view.setBbox(layer)

      // then (order is minX,maxX,maxY,minY = west,east,north,south)
      expect(view.model.get('bbox')).toEqual('-5.032,0.286,61.260,59.255')
    })
  })

  describe('bbox from the model is drawn on init', () => {
    it('draws the rectangle when the model already has a bbox (direct URL load)', () => {
      // given a model constructed with a bbox already set, as on a fresh URL load
      const view = newView({ bbox: '-5.032,0.286,61.260,59.255' })

      // then the rectangle has been drawn without waiting for a change event
      expect(view.drawnItems.getLayers().length).toEqual(1)
    })

    it('draws nothing when the model has no bbox', () => {
      // given
      const view = newView({})

      // then
      expect(view.drawnItems.getLayers().length).toEqual(0)
    })
  })
})
