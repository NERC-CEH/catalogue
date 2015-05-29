define [
  'cs!views/editor/ResourceIdentifiersItemView',
  'cs!models/editor/ResourceIdentifier'
], (View, Model) ->
  describe 'ResourceIdentifiersItemView', ->
    view = null
    model = new Model()

    beforeEach ->
      view = new View model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is initialized without model', ->
      it 'should throw exception', ->
        expect(() -> new View()).toThrow(new Error('model is required'))

    describe 'when view is rendered', ->

      beforeEach ->
        do view.render

      it 'there should be html', ->
        expect(view.el.html).not.toBeNull()

      describe 'adding new resource identifier', ->

        beforeEach: ->
          spyOn view.resourceIdentifiers, 'add'

        it 'be added to collection', ->
          view.$('#resourceIdentifierAdd').trigger 'click'
          expect(view.resourceIdentifiers.add).toHaveBeenCalled()