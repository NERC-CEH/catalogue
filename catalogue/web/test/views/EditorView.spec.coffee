define [
  'jquery'
  'cs!models/GeminiMetadata'
  'cs!views/GeminiEditorView'
  'bootstrap'
], ($, Metadata, EditorView) ->
  describe 'EditorView', ->
    view = null
    model = new Metadata()

    beforeEach ->
      view = new EditorView
        model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when exit clicked', ->

      beforeEach ->
        spyOn(view, 'exit')
        do view.delegateEvents
        do view.render

      it 'exit clicked', ->
        view.$('#editorExit').trigger 'click'
        expect(view.exit).toHaveBeenCalled()

    describe 'when next clicked', ->

      beforeEach ->
        spyOn(view, 'next')
        do view.delegateEvents
        do view.render

      it 'next method called', ->
        view.$('#editorNext').trigger 'click'
        expect(view.next).toHaveBeenCalled()

    describe 'when back clicked', ->

      beforeEach ->
        spyOn(view, 'back')
        do view.delegateEvents
        do view.render

      it 'back clicked', ->
        view.$('#editorBack').prop("disabled", false).trigger 'click'
        expect(view.back).toHaveBeenCalled()

    describe 'when save clicked', ->

      beforeEach ->
        spyOn(view, 'save').and.callThrough()
        spyOn(model, 'save')
        do view.delegateEvents
        do view.render

      it 'clicking Save calls method', ->
        view.$('#editorSave').prop("disabled", false).trigger 'click'
        expect(view.save).toHaveBeenCalled()
        expect(model.save).toHaveBeenCalled()

    describe 'when delete clicked', ->

      beforeEach ->
        spyOn(model, 'destroy')
        do view.render

      it 'clicking Delete calls method', ->
        view.$('#confirmDeleteYes').trigger 'click'
        expect(model.destroy).toHaveBeenCalled()
