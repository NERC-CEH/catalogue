define [
  'jquery'
  'cs!views/SearchAppView'
  'cs!models/SearchApp'
], ($, SearchAppView, SearchApp) ->
  describe "SearchAppView", ->
    el = null
    model = null
    view = null

    beforeEach ->
      el = $('<div></div>').appendTo $('body')
      model = new SearchApp
      view = new SearchAppView el: el, model: model

    afterEach ->
      do el.remove
    
    it "sets the state back to default when a plain url is clicked", ->
      spyOn model, 'setState'
      a = $("<a href='#{view.appUrl}'></a>").appendTo el
      a.trigger 'click'

      expect(model.setState).toHaveBeenCalledWith {}

    it "sets the state based on the params in the url when clicked", ->
      spyOn model, 'setState'
      a = $("<a href='#{view.appUrl}?some=state'></a>").appendTo el
      a.trigger 'click'

      expect(model.setState).toHaveBeenCalledWith some: 'state'

    it "enables map search when .map-heading clicked", ->
      spyOn model, 'set'
      heading = $('<div class="map-heading"><h3></h3></div>').appendTo el
      $('h3', el).trigger 'click'

      expect(model.set).toHaveBeenCalledWith 'mapsearch', true

    it "disabled map search when .facet-heading clicked", ->
      spyOn model, 'set'
      heading = $('<div class="facet-heading"><h3></h3></div>').appendTo el
      $('h3', el).trigger 'click'

      expect(model.set).toHaveBeenCalledWith 'mapsearch', false