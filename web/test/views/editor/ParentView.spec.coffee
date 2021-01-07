define [
  'cs!views/editor/ParentView'
  'cs!models/EditorMetadata'
  'cs!views/editor/ResourceIdentifierView'
], (ParentView, EditorMetadata, ResourceIdentifierView) ->
  describe 'ParentView', ->
    view = model = null

    beforeEach ->
      model = new EditorMetadata()
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

    describe 'with nested depth', ->
      beforeEach ->
        view = new ParentView
          model: model
          modelAttribute: 'one.two.depth'
          label: 'Resource Identifiers'
          ObjectInputView: ResourceIdentifierView
          helpText: """
                    <p>Test</p>
                    """

      it 'can getModelData', ->
        model.set one: two: depth: ['value']
        expect(view.getModelData()).toEqual ['value']

      it 'can supply empty getModelData', ->
        expect(view.getModelData()).toEqual []

      it 'can populate model', ->
        view.collection.add test: 'data'
        expect(model.attributes.one.two.depth).toEqual [{test: 'data'}]

    describe 'when view is created with populated model', ->

      beforeEach ->
        model = new EditorMetadata
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

      it 'should be populated from EditorMetadata model', ->
        identifier = view.collection.at 0
        expect(identifier.get 'code').toEqual '1234'

    describe 'clicking add', ->

      beforeEach ->
        spyOn view.collection, 'add'

      it 'should add new model to collection', ->
        view.$('.add').trigger 'click'
        expect(view.collection.add).toHaveBeenCalled()