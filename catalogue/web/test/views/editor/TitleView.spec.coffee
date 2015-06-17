define [
  'jquery'
  'cs!models/Metadata'
  'cs!views/editor/TitleView'
], ($, Metadata, TitleView) ->
  describe 'TitleView', ->
    view = null
    model = new Metadata()
    inputField = '#input-title'

    beforeEach ->
      view = new TitleView
        model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is initialized without model', ->
      it 'should throw exception', ->
        expect(() -> new TitleView()).toThrow(new Error('model is required'))

    describe 'when view is rendered', ->

      beforeEach ->
        do view.render

      it 'title field should be empty', ->
        expect(view.$(inputField).val()).toEqual ''

      describe 'when title is changed', ->

        beforeEach ->
          spyOn(view, 'updateModel')
          do view.delegateEvents

        it 'model should be updated', ->
          view.$(inputField).val('new value').trigger 'change'
          expect(view.updateModel).toHaveBeenCalled()

      describe 'when updating model', ->

        beforeEach ->
          spyOn(model, 'set')
          view.$(inputField).val 'new value'

        it 'should set value on model', ->
          do view.updateModel
          expect(model.set).toHaveBeenCalledWith 'title', 'new value'

    describe 'when view is rendered with populated model', ->

      beforeEach ->
        model = new Metadata
          title: 'Countryside Survey'
        view = new TitleView
          model: model
        do view.render

      it 'input field should contain model value', ->
        expect(view.$(inputField).val()).toEqual 'Countryside Survey'