define [
  'cs!models/SearchPage'
], (SearchPage) ->
  describe "SearchPage", ->
    it "can locate a selected results", ->
      selected   = identifier: '17', name: 'selected'
      unselected = identifier: '19', name: 'something else'
      page = new SearchPage 
        selected: '17'
        results: [unselected, selected]

      expect(page.getSelectedResult().name).toBe 'selected'

    it "doesn't return a selected result if nothing is selected", ->
      unselected = identifier: '100', name: 'only unselected result'
      page = new SearchPage 
        results: [unselected]

      expect(page.getSelectedResult()).toBe undefined

    it "doesn't return a selected if there are no results", ->
      page = new SearchPage 

      expect(page.getSelectedResult()).toBe undefined