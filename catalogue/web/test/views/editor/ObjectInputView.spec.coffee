define [
  'cs!views/editor/InputView'
  'cs!models/GeminiMetadata'
], (InputView, GeminiMetadata) ->
  describe 'InputView', ->
    model = null
    view = null

    beforeEach ->
      model = new GeminiMetadata title: 'some text'
      view = new InputView model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()