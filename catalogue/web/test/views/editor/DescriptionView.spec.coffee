define [
  'jquery'
  'cs!models/Metadata'
  'cs!views/editor/DescriptionView'
], ($, Metadata, DescriptionView) ->
  describe 'DescriptionView', ->
    view = null
    model = new Metadata()
    inputField = '#input-description'

    beforeEach ->
      view = new DescriptionView
        model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is initialized without model', ->
      it 'should throw exception', ->
        expect(() -> new DescriptionView()).toThrow(new Error('model is required'))

    describe 'when view is rendered', ->

      beforeEach ->
        do view.render

      it 'desciption field should be empty', ->
        expect(view.$(inputField).val()).toEqual ''

      describe 'when description is changed', ->

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
          expect(model.set).toHaveBeenCalledWith 'description', 'new value'

    describe 'when view is rendered with populated model', ->

      beforeEach ->
        model = new Metadata
          description: 'Countryside Survey'
        view = new DescriptionView
          model: model
        do view.render

      it 'input field should contain model value', ->
        expect(view.$(inputField).val()).toEqual 'Countryside Survey'