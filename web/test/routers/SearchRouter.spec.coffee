define [
  'cs!routers/SearchRouter'
  'backbone'
], (SearchRouter, Backbone) ->
  describe "SearchRouter", ->
    it "sets the state with the query string content if no fragment is present", ->
      location = hash: '', search: '?state=something&here=is'

      model = new Backbone.Model
      model.setState = jasmine.createSpy 'setState'

      new SearchRouter model:model, location: location

      expect(model.setState).toHaveBeenCalledWith 
          state: 'something'
          here: 'is'
        , silent: true

    it "sets the state on the model when updateModel is called", ->
      model = new Backbone.Model
      model.setState = jasmine.createSpy 'setState'

      router = new SearchRouter model:model, location: location
      router.updateModel 'state=hello'

      expect(model.setState).toHaveBeenCalledWith state: 'hello', undefined

    it "can turn the state of the model in to a route", ->
      model = new Backbone.Model state: 'myState'
      model.getState =-> state: 'myState'

      router = new SearchRouter model:model, location: {hash: 'somehash'}
      spyOn router, 'navigate'

      do router.updateRoute

      expect(router.navigate).toHaveBeenCalledWith 'state=myState', replace: true

    it "can turn the array state of the model in to a route", ->
      model = new Backbone.Model
      model.getState =-> state: ['myState', 'andAnother']

      router = new SearchRouter model:model, location: {hash: 'somehash'}
      spyOn router, 'navigate'

      do router.updateRoute

      expect(router.navigate).toHaveBeenCalledWith 'state=myState&state=andAnother', replace: true
