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
    
    it "sets the locations defined in wktLiterals", ->
      el.html '''
        <span content="POLYGON((-1 -9, -1 9, 1 9, 1 -9, -1 -9))" datatype="geo:wktLiteral"></span>
        <span content="POLYGON((-1 -8, -1 8, 1 8, 1 -8, -1 -8))" datatype="geo:wktLiteral"></span>'''
      view = new StudyAreaView el: el

      expect(view.highlightedLayer.features.length).toBe 4
