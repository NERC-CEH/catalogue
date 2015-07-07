define [
  'backbone'
  'cs!views/editor/ChildView'
  'cs!views/editor/TopicCategoryView'
  'cs!models/editor/TopicCategory'
], (Backbone, ChildView, TopicCategoryView, TopicCategory) ->

  describe 'ChildView', ->
    view = model = collection = null

    beforeEach ->
      model = new TopicCategory()
      collection = new Backbone.Collection [model]
      view = new ChildView
        model: model
        ModelType: TopicCategory
        modelAttribute: 'topicCategories'
        label: 'Topic Categories'
        ObjectInputView: TopicCategoryView
        helpText: """
                  <p>Help</p>
                  """

    describe 'when view is constructing', ->
      it 'should exist', ->
        expect(view).toBeDefined()

    describe 'remove contact', ->

      beforeEach ->
        spyOn view, 'remove'
        do view.delegateEvents
        do view.render

      it 'click remove', ->
        view.$('button.remove').trigger 'click'
        expect(view.remove).toHaveBeenCalled()
