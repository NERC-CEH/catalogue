define [
  'jquery'
  'cs!models/EditorApp'
  'cs!views/EditorAppView'
], ($, EditorApp, EditorAppView) ->
  describe 'EditorAppView', ->
    view = null
    model = new EditorApp()

    beforeEach ->
      view = new EditorAppView
        model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is initialized without model', ->
      it 'should throw exception', ->
        expect(() -> new EditorAppView()).toThrow(new Error('model is required'))