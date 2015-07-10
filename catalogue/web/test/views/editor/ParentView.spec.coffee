define [
  'cs!views/editor/ParentView'
  'cs!models/GeminiMetadata'
  'cs!views/editor/ResourceIdentifierView'
], (ParentView, GeminiMetadata, ResourceIdentifierView) ->
  describe 'ParentView', ->
    view = model = null

    beforeEach ->
      model = new GeminiMetadata()
      view = new ParentView
        model: model
        modelAttribute: 'resourceIdentifiers'
        label: 'Resource Identifiers'
        ObjectInputView: ResourceIdentifierView
        helpText: """
                  <p>Test</p>
                  """

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is created with populated model', ->

      beforeEach ->
        model = new GeminiMetadata
          resourceIdentifiers: [
              code: '1234'
              codeSpace: 'CEH:EIDC:'
            ,
              code: '10.5285/1e7d5e08-9e24-471b-ae37-49b477f695e3'
              codeSpace: 'doi:'
          ]

        view = new ParentView
          model: model
          modelAttribute: 'resourceIdentifiers'
          label: 'Resource Identifiers'
          ObjectInputView: ResourceIdentifierView
          helpText: """
                    <p>Test</p>
                    """
        do view.render

      it 'should be a resourceIdentifiers collection', ->
        expect(view.collection).toBeDefined()

      it 'should be populated from GeminiMetadata model', ->
        identifier = view.collection.at 0
        expect(identifier.get 'code').toEqual '1234'

    describe 'clicking add', ->

      beforeEach ->
        spyOn view.collection, 'add'

      it 'should add new model to collection', ->
        view.$('.add').trigger 'click'
        expect(view.collection.add).toHaveBeenCalled()