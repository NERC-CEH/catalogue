define [
  'jquery'
  'cs!views/DrawingControlView'
  'cs!models/SearchApp'
], ($, DrawingControlView, SearchApp) ->
  describe "DrawingControlView", ->
    el = null
    model = null

    beforeEach ->
      el = $('<div></div>').appendTo $('body')
      model = new SearchApp

    afterEach ->
      do el.remove
    
    it "shows a pencil when no link to remove bounding box exists", ->
      view = new DrawingControlView model: model, el: el

      expect($('#drawing-toggle span', el).hasClass 'fa-pencil' ).toBe true

    it "shows a cross when link to remove bounding box exists", ->
      view = new DrawingControlView model: model, el: el
      model.results.set withoutBbox: 'http://remove-link'

      expect($('a span', el).hasClass 'fa-times' ).toBe true

    it "shows a spatial operation dropdown when bounding box exists", ->
      view = new DrawingControlView model: model, el: el
      model.results.set withoutBbox: 'http://remove-link'

      expect($('#spatial-op-dropdown', el).length).toBe 1

    it "doesn't show a spatial operation when not spatial filtering", ->
      view = new DrawingControlView model: model, el: el
      expect($('#spatial-op-dropdown', el).length).toBe 0

    describe "drawing toggle", ->
      it "switches to map search mode when clicked", ->
        view = new DrawingControlView model: model, el: el
        do $('#drawing-toggle', el).click

        expect(model.get 'mapsearch').toBe true

      it "turns drawing on when turned off", ->
        model.set 'drawing', false
        view = new DrawingControlView model: model, el: el
        do $('#drawing-toggle', el).click

        expect(model.get 'drawing').toBe true
        expect($('#drawing-toggle', el).hasClass 'active' ).toBe true

      it "turns drawing of when turned on", ->
        model.set 'drawing', true
        view = new DrawingControlView model: model, el: el
        do $('#drawing-toggle', el).click

        expect(model.get 'drawing').toBe false
        expect($('#drawing-toggle', el).hasClass 'active' ).toBe false

    describe "spatial dropdown", ->
      it "shows Overlapping when link to withinBbox is present", ->
        view = new DrawingControlView model: model, el: el
        model.results.set withoutBbox: 'http://remove-link'
        model.results.set withinBbox: 'http://entirelyWithin-link'

        expect($("#spatial-op-dropdown:contains('Overlapping')", el).length).toBe 1
        expect($("#spatial-op-dropdown:contains('Entirely Within')", el).length).toBe 0

      it "shows Entirely Within when link to intersectingBbox is present", ->
        view = new DrawingControlView model: model, el: el
        model.results.set withoutBbox: 'http://remove-link'
        model.results.set intersectingBbox: 'http://intersecting-link'

        expect($("#spatial-op-dropdown:contains('Overlapping')", el).length).toBe 0
        expect($("#spatial-op-dropdown:contains('Entirely Within')", el).length).toBe 1

      it "has links in dropdown to modes", ->
        view = new DrawingControlView model: model, el: el
        model.results.set withoutBbox: 'http://remove-link'
        model.results.set url: 'http://these-results'
        model.results.set intersectingBbox: 'http://intersecting-link'

        expect($("a[href='http://intersecting-link']:contains('Overlapping')", el).length).toBe 1
        expect($("a[href='http://these-results']:contains('Entirely Within')", el).length).toBe 1
