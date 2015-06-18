define [
  'jquery'
  'cs!models/editor/TopicCategory'
  'cs!views/editor/TopicCategoriesItemView'
], ($, TopicCategory, TopicCategoriesItemView) ->
  describe 'TopicCategoriesItemView', ->
    view = null
    model = new TopicCategory()

    beforeEach ->
      view = new TopicCategoriesItemView
        model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is initialized without model', ->
      it 'should throw exception', ->
        expect(() -> new TopicCategoriesItemView()).toThrow(new Error('model is required'))

    describe 'rendered view', ->

      beforeEach ->
        do view.render

      describe 'remove topic category', ->

        beforeEach ->
          spyOn view, 'remove'
          do view.delegateEvents
          do view.render

        it 'click remove', ->
          view.$('button').trigger 'click'

          expect(view.remove).toHaveBeenCalled()

      describe 'modify topic category', ->

        beforeEach ->
          spyOn view, 'modify'
          do view.delegateEvents
          do view.render

        it 'change value', ->
          view.$('select').trigger 'change'

          expect(view.modify).toHaveBeenCalled()