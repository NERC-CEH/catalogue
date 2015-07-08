define [
  'backbone'
  'cs!collections/Positionable'
], (Backbone, Positionable) ->
  describe "Positionable", ->
    it "can reposition which have already been added", ->
      collection = new Positionable
      model1 = new Backbone.Model
      model2 = new Backbone.Model
      collection.add [model1, model2]

      collection.position 0, 1

      expect(collection.at(1)).toBe model1
      expect(collection.at(0)).toBe model2