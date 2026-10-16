import { describe, expect, it, vi } from 'vitest'
import SpatialFilterView from '../src/views/SpatialFilterView'
import SearchApp from '../src/SearchApp'
import $ from 'jquery'
import L from 'leaflet'

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

  describe('drawing a search box', () => {
    const drag = (view, from, to) => {
      const at = latlng => ({ latlng, originalEvent: { clientX: 0, clientY: 0 } })
      view.map.fire('mousedown', at(from))
      // More than one move, as a hand makes: the box exists only after the first, and its area is
      // shown from the second
      view.map.fire('mousemove', at(L.latLng((from.lat + to.lat) / 2, (from.lng + to.lng) / 2)))
      view.map.fire('mousemove', at(to))
      // Leaflet.draw finishes the shape on the document's mouseup
      document.dispatchEvent(new MouseEvent('mouseup'))
    }
    const rectangleTool = view => view.drawControl._toolbars.draw._modes.rectangle.handler

    it('makes a box from the corner pressed to the corner released', () => {
      // given the rectangle tool picked
      const view = newView({})
      view.showMap()
      rectangleTool(view).enable()

      // when the user drags from one corner to the other. The tooltip's area, shown as it is
      // dragged, threw in Leaflet.draw's own code under an ES module's strict mode, so the box
      // stopped where it was first pressed
      drag(view, L.latLng(50, -5), L.latLng(55, 1))

      // then
      expect(view.model.get('bbox')).toEqual('-5.000,1.000,55.000,50.000')
    })

    it('hides the results while the box is drawn, and shows them again after', () => {
      // given the results shown
      const view = newView({})
      view.showMap()

      // when the tool is picked
      rectangleTool(view).enable()

      // then they are out of the way
      expect(view.map.hasLayer(view.resultLayers)).toBe(false)

      // when the box is drawn
      drag(view, L.latLng(50, -5), L.latLng(55, 1))

      // then
      expect(view.map.hasLayer(view.resultLayers)).toBe(true)
    })

    it('shows the results again when the tool is put down without drawing', () => {
      // given
      const view = newView({})
      view.showMap()
      rectangleTool(view).enable()

      // when
      rectangleTool(view).disable()

      // then
      expect(view.map.hasLayer(view.resultLayers)).toBe(true)
    })

    it('hides the results while the delete tool is picked, as they lie over the box to delete', () => {
      // given a box drawn
      const view = newView({ bbox: '-5.000,1.000,55.000,50.000' })
      view.showMap()
      const deleteTool = view.drawControl._toolbars.edit._modes.remove.handler

      // when
      deleteTool.enable()

      // then
      expect(view.map.hasLayer(view.resultLayers)).toBe(false)

      // when it is put down
      deleteTool.disable()

      // then
      expect(view.map.hasLayer(view.resultLayers)).toBe(true)
    })

    it('leaves the results hidden if they were turned off before drawing', () => {
      // given the results layer turned off in the layer control
      const view = newView({})
      view.showMap()
      view.map.removeLayer(view.resultLayers)

      // when a box is drawn
      rectangleTool(view).enable()
      drag(view, L.latLng(50, -5), L.latLng(55, 1))

      // then
      expect(view.map.hasLayer(view.resultLayers)).toBe(false)
    })
  })

  it('fits the map to its box when the box is resized, as the filters are dragged wider', async () => {
    // given
    const view = newView({})
    view.$('.map').css({ width: '300px', height: '300px' })
    view.showMap()
    const fitted = vi.spyOn(view.map, 'invalidateSize')

    // when
    view.$('.map').css('width', '600px')
    await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))

    // then
    expect(fitted).toHaveBeenCalled()
    expect(view.map.getSize().x).toBe(600)
  })

  it('stops fitting the map once it has been removed', async () => {
    // given a map shown, then removed
    const view = newView({})
    view.$('.map').css({ width: '300px', height: '300px' })
    view.showMap()
    const map = view.map
    map.remove()
    const fitted = vi.spyOn(map, 'invalidateSize')

    // when its box changes size, as when the page around it is emptied. A removed map has no panes
    // left to measure, and fitting it threw (CI, !1315)
    view.$('.map').css('width', '600px')
    await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))

    // then
    expect(fitted).not.toHaveBeenCalled()
  })

  describe('bbox from the model is drawn once the map is shown', () => {
    it('draws the rectangle when the model already has a bbox (direct URL load)', () => {
      // given a model constructed with a bbox already set, as on a fresh URL load
      const view = newView({ bbox: '-5.032,0.286,61.260,59.255' })

      // when its tab is first shown
      view.showMap()

      // then the rectangle has been drawn without waiting for a change event
      expect(view.drawnItems.getLayers().length).toEqual(1)
    })

    it('draws nothing when the model has no bbox', () => {
      // given
      const view = newView({})

      // when
      view.showMap()

      // then
      expect(view.drawnItems.getLayers().length).toEqual(0)
    })
  })
})
