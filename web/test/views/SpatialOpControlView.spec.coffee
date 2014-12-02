define [
  'jquery'
  'cs!views/SpatialOpControlView'
  'cs!models/SearchApp'
], ($, SpatialOpControlView, SearchApp) ->
  describe "SpatialOpControlView", ->
    el = null
    model = null

    beforeEach ->
      el = $('<div></div>').appendTo $('body')
      model = new SearchApp

    afterEach ->
      do el.remove

    it "button is disabled when bbox undefined", ->
      view = new SpatialOpControlView model: model, el: el
      expect($('.dropdown-toggle', el).prop 'disabled').toBe true

    it "button is enabled when bbox defined", ->
      view = new SpatialOpControlView model: model, el: el
      model.set 'bbox', '1234'
      expect($('.dropdown-toggle', el).prop 'disabled').toBe false

    it "model op is update when dropdown clicked", ->
      view = new SpatialOpControlView model: model, el: el
      do $('.dropdown-menu li:first a', el).click
      expect(model.get 'op').toEqual 'iswithin'

    it "updating model op updates button text", ->
      view = new SpatialOpControlView model: model, el: el
      model.set 'op', 'intersects'
      expect(do $('#spatialOp', el).text).toEqual 'that overlap'