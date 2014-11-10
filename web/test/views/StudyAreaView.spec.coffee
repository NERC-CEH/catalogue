define [
  'jquery'
  'cs!views/StudyAreaView'
], ($, StudyAreaView) ->
  describe "StudyAreaView", ->
    el = null

    beforeEach ->
      el = $('<div></div>').appendTo $('body')

    afterEach ->
      do el.remove
    
    it "sets the locations defined in data-location", ->
      el.attr 'data-location', '1 2 3 4,3 4 5 6'
      view = new StudyAreaView el: el

      expect(view.highlightedLayer.features.length).toBe 4