define [
  'cs!models/SearchApp'
], (SearchApp, MetadataDocument) ->
  describe "SearchApp", ->
    app = null

    beforeEach ->
      app = new SearchApp

    it "jumps to the default page if anything changes", ->
      app = new SearchApp page: 10

      app.set 'term', 'hello'

      expect(app.get 'page').toBe app.defaults.page


    it "disables drawing if anything changes", ->
      app = new SearchApp drawing: true

      app.set 'term', 'hello'

      expect(app.get 'drawing').toBe app.defaults.drawing

    it "can get the current results set", ->
      app = new SearchApp
      expect(app.getResults()).toBe app.results

    it "can clear results even if results are not set", ->
      app = new SearchApp
      app.results = null
      spyOn app, 'trigger'

      do app.clearResults

      expect(app.trigger).toHaveBeenCalledWith 'cleared:results'

    it "stops listening to the results set when clearing", ->
      app = new SearchApp
      results = app.results
      spyOn app, 'trigger'
      spyOn results, 'off'

      do app.clearResults

      expect(results.off).toHaveBeenCalled()
      expect(app.trigger).toHaveBeenCalledWith 'cleared:results'

    it "nulls the results when clearing", ->
      app = new SearchApp
      spyOn app, 'trigger'

      do app.clearResults
      expect(app.results).toBeNull()
      expect(app.trigger).toHaveBeenCalledWith 'cleared:results'

    it "can get the state which is relevant to searching", ->
      app = new SearchApp drawing:true, term: 'my term'

      expect(app.getState()).toEqual jasmine.objectContaining term: 'my term'
      expect(app.getState()).not.toEqual jasmine.objectContaining drawing: true

    it "can use defaults for missing parts of state", ->
      app = new SearchApp term: 'my term'
      app.setState page:2

      expect(app.getState().term).toBe app.defaults.term

    it "proxies search replaces search results when searching", ->
      app = new SearchApp
      firstpage = app.results

      app.set 'term', 'trigger search'

      expect(app.results).not.toBeNull()
      expect(firstpage).not.toBe app.results

    it "clears results when performing a search", ->
      app = new SearchApp
      spyOn app, 'clearResults'

      app.set 'term', 'clear the results first'

      expect(app.clearResults).toHaveBeenCalled()

    it "fetches a search page from the server", ->
      app = new SearchApp
      searching = jasmine.createSpy 'searching'
      app.on 'results-request', searching

      app.set 'term', 'start searching please'

      expect(searching).toHaveBeenCalled()

    it "does not listen to the events of old searches", ->
      app = new SearchApp
      events = jasmine.createSpy 'events'
      oldresults = app.results
      app.on 'results-dummy', events
      do app.clearResults

      oldresults.trigger 'dummy'

      expect(events).not.toHaveBeenCalled()
      