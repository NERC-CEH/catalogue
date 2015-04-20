define [
  'jquery'
  'cs!views/SearchFormView'
], ($, SearchFormView) ->
  describe "SearchFormView", ->
    el = null

    beforeEach ->
      el = $('''
        <form method="get">
          <input name="term" type="text">
        </form>''').appendTo $('body')

    afterEach ->
      do el.remove
    
    it "updates the term on the model when form submitted", ->
      model = new Backbone.Model
      view = new SearchFormView el: el, model: model

      $("[name='term']", el).val 'my term'
      el.trigger 'submit'
      expect(model.get 'term').toBe 'my term'

    it "hides the results when typing starts", ->
      model = new Backbone.Model
      model.clearResults = jasmine.createSpy 'clearResults'

      view = new SearchFormView el: el, model: model
      $("[name='term']", el).val 'my term'
      $('input',el).trigger 'keyup'

      expect(model.clearResults).toHaveBeenCalled()
