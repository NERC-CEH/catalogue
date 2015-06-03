define [
  'cs!views/editor/ResourceIdentifiersView'
  'cs!models/Metadata'
], (View, Metadata) ->
  describe 'ParentView', ->
    view = null
    model = new Metadata()

    beforeEach ->
      view = new View model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

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
        expect(view.collection).toBeDefined()

      it 'should be populated from metadata model', ->
        identifier = view.collection.at 0
        expect(identifier.get 'code').toEqual '1234'