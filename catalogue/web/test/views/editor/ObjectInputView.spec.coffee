define [
  'cs!views/editor/InputView'
  'cs!models/editor/String'
], (InputView, String) ->
  describe 'ObjectInputView', ->
    model = null
    view = null

    beforeEach ->
      model = new String value: 'some text'
      view = new InputView model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()