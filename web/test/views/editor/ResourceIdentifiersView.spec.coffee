define [
  'cs!views/editor/ResourceIdentifiersView'
  'cs!models/Metadata'
], (View, Metadata) ->
  describe 'ResourceIdentifiersView', ->
    view = null
    model = new Metadata()

    beforeEach ->
      view = new View model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is initialized without model', ->
      it 'should throw exception', ->
        expect(() -> new View()).toThrow(new Error('model is required'))

    describe 'when view is created with populated model', ->

      beforeEach ->
        model = new Metadata
          resourceIdentifiers: [
              code: '1234'
              codeSpace: 'CEH:EIDC:'
            ,
              code: '10.5285/1e7d5e08-9e24-471b-ae37-49b477f695e3'
              codeSpace: 'doi:'
          ]

        view = new View
          model: model
        do view.render

      it 'should be a resourceIdentifiers collection', ->
        expect(view.resourceIdentifiers).toBeDefined()

      it 'should be populated from metadata model', ->
        identifier = view.resourceIdentifiers.at 0
        expect(identifier.get 'code').toEqual '1234'

      describe 'collection changes', ->

        beforeEach ->
          spyOn model, 'set'

        it 'should update Metadata model', ->
          view.resourceIdentifiers.trigger 'add'
          expect(model.set).toHaveBeenCalled()

      describe 'collection changes', ->

        beforeEach ->
          do view.resourceIdentifiers.reset
          spyOn model, 'unset'

        it 'should unset Metadata model', ->
          view.resourceIdentifiers.trigger 'remove'
          expect(model.unset).toHaveBeenCalled()

      describe 'render collection', ->

        it 'should render all ResourceIdentifiers', ->
          expect(view.el.html).not.toBeNull()