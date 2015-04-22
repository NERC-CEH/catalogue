define [
  'jquery'
  'cs!models/Metadata'
  'cs!views/EditorView'
], ($, Metadata, EditorView) ->
  describe 'EditorView', ->
    view = null
    model = new Metadata()

    beforeEach ->
      view = new EditorView
        model: model
        parent: {}

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is initialized without model', ->
      it 'should throw exception', ->
        expect(() -> new EditorView()).toThrow(new Error('model is required'))

    describe 'when view is initialized without parent model', ->
      it 'should throw exception', ->
        expect(() -> new EditorView
          model: model
        ).toThrow(new Error('parent is required'))

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
        view.$('#editorSave').trigger 'click'
        expect(view.save).toHaveBeenCalled()
        expect(model.save).toHaveBeenCalled()

    describe 'when Step 4 clicked', ->

      beforeEach ->
        spyOn(view, 'direct')
        do view.delegateEvents
        do view.render

      it 'direct method called', ->
        view.$('#editorNav li:eq(4)').trigger 'click'
        expect(view.direct).toHaveBeenCalled()
