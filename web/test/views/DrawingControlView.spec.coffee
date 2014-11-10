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

      expect($('button span', el).hasClass 'glyphicon-pencil' ).toBe true

    it "shows a cross when link to remove bounding box exists", ->
      view = new DrawingControlView model: model, el: el
      model.results.set withoutBBox: 'http://remove-link'

      expect($('a span', el).hasClass 'glyphicon-remove' ).toBe true

    
    describe "drawing toggle", ->
      it "switches to map search mode when clicked", ->
        view = new DrawingControlView model: model, el: el
        do $('button', el).click

        expect(model.get 'mapsearch').toBe true

      it "turns drawing on when turned off", ->
        model.set 'drawing', false
        view = new DrawingControlView model: model, el: el
        do $('button', el).click

        expect(model.get 'drawing').toBe true
        expect($('button', el).hasClass 'active' ).toBe true

      it "turns drawing of when turned on", ->
        model.set 'drawing', true
        view = new DrawingControlView model: model, el: el
        do $('button', el).click

        expect(model.get 'drawing').toBe false
        expect($('button', el).hasClass 'active' ).toBe false