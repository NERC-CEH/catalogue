define [
  'cs!views/editor/InputView'
  'cs!models/EditorMetadata'
], (InputView, EditorMetadata) ->
  describe 'InputView', ->
    model = null
    view = null

    beforeEach ->
      model = new EditorMetadata title: 'some text'
      view = new InputView model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()