define [
  'jquery'
  'cs!models/Metadata'
  'cs!views/editor/TopicCategoriesView'
] , ($, Metadata, TopicCategoriesView) ->
  describe 'TopicCategoriesView', ->
    view = null
    model = new Metadata()

    beforeEach ->
      view = new TopicCategoriesView
        model: model

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'when view is initialized without model', ->
      it 'should throw exception', ->
        expect(() -> new TopicCategoriesView()).toThrow(new Error('model is required'))

    describe 'when view is rendered with populated model', ->

      beforeEach ->
        model = new Metadata
          topicCategories: [
            value: 'inlandWaters'
            uri: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/inlandWaters'
          ,
            value: 'biota'
            uri: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/biota'
          ]

        view = new TopicCategoriesView
          model: model
        do view.render

      it 'there should be 2 items in the collection', ->
        expect(view.topicCategories.length).toEqual 2

    describe 'adding new Topic Category', ->

      beforeEach ->
        spyOn view, 'add'
        do view.delegateEvents
        do view.render

      it 'should call add', ->
        view.$('#topicCategory').trigger 'change'

        expect(view.add).toHaveBeenCalled()

    describe 'adding new Topic Category to model', ->

      beforeEach ->
        spyOn model, 'set'
        view = new TopicCategoriesView
          model: model
        do view.render

      it 'should set model', ->
        view.$('#topicCategory').val('inlandWaters').trigger 'change'

        expect(model.set).toHaveBeenCalled()