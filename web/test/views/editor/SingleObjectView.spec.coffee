define [
  'cs!models/EditorMetadata'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/InputView'
  'cs!models/editor/String'
], (EditorMetadata, SingleObjectView, InputView, String) ->
  describe 'SingleObjectView', ->
    view = null
    model = new EditorMetadata()

    beforeEach ->
      view = new SingleObjectView
        model: model
        modelAttribute: 'title'
        ModelType: String
        label: 'Title'
        ObjectInputView: InputView,
        helpText: """
                  <p>Test</p>
                  """

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

#    describe 'edit title', ->
#
#      beforeEach ->
#        spyOn view, 'updateMetadataModel'
#        do view.delegateEvents
#        do view.render
#
#      it 'should update model', ->
#        view.$('#input-title').trigger 'change'
#
#        expect(view.updateMetadataModel).toHaveBeenCalled()