define [
  'jquery'
  'cs!views/FacetsPanelView'
  'cs!models/SearchApp'
], ($, FacetsPanelView, SearchApp) ->
  describe "FacetsPanelView", ->
    el = null
    model = null

    beforeEach ->
      el = $('<div></div>').appendTo $('body')
      model = new SearchApp

    afterEach ->
      do el.remove
    
    it "renders when results are synced", ->
      view = new FacetsPanelView model: model, el: el
      spyOn(el, 'html')
      model.trigger 'results-sync'

      expect(el.html).toHaveBeenCalled()