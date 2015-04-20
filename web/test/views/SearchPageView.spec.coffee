define [
  'jquery'
  'cs!views/SearchPageView'
  'cs!models/SearchApp'
], ($, SearchPageView, SearchApp) ->
  describe "SearchPageView", ->
    el = null
    model = null

    beforeEach ->
      el = $('<div></div>').appendTo $('body')
      model = new SearchApp

    afterEach ->
      do el.remove
    
    it "empties the dom when cleared", ->
      view = new SearchPageView model: model, el: el
      spyOn(el, 'empty')
      model.trigger 'cleared:results'

      expect(el.empty).toHaveBeenCalled()

    it "pads so we can scroll to the bottom", ->
      view = new SearchPageView model: model, el: el
      result = $('<div class="result"></div>').css height:20
      el.append result
      do view.padResults

      expect(el.css 'marginBottom').toBe "#{$(window).height() - 20}px"

    it "can populate the model from the dom", ->
      result = $('''
        <div class="result" id="27" data-location="1 2 3 4,5 6 7 8">
          <h1 class="title">mr title</h1>
          <p class="description">whatever</p>
        </div>
      ''')
      el.append result

      view = new SearchPageView model: model, el: el

      results = model.getResults().attributes.results
      expect(results.length).toBe 1
      expect(results).toContain 
        identifier:  "27"
        title:       "mr title"
        description: "whatever"
        locations:   ["1 2 3 4", "5 6 7 8"]