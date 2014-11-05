define [
  'jquery'
  'backbone'
  'cs!views/SortableCollectionView'
], ($, Backbone, SortableCollectionView) ->
  describe "SortableCollectionView", ->
    el = null
    collection = null
    view = null

    beforeEach ->
      el = $('<ul></ul>').appendTo $('body')
      collection = new Backbone.Collection
      view = new SortableCollectionView 
        el: el
        collection : collection
        attributes: 
          subView: Backbone.View

    afterEach ->
      do el.remove
    
    it "creates subview for newly added model", ->
      model = new Backbone.Model
      collection.add model

      expect(model._subView).toBeTruthy()
      expect(el.children().length).toBe 1

    it "remove sub view when model is removed", ->
      model = new Backbone.Model
      collection.add model
      spyOn(model._subView, 'remove').and.callThrough()

      collection.remove model
      expect(model._subView.remove).toHaveBeenCalled()
      expect(el.children().length).toBe 0

    it "replaces views on reset", ->
      old = new Backbone.Model
      newOne = new Backbone.Model
      collection.add old
      spyOn(old._subView, 'remove').and.callThrough()

      collection.reset [newOne]
      expect(old._subView.remove).toHaveBeenCalled()
      expect(newOne._subView).toBeTruthy()
      expect(el.children().length).toBe 1