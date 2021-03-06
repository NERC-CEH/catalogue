define [
  'underscore'
  'backbone'
  'cs!views/editor/ObjectInputView'
  'cs!views/editor/ChildView'
  'cs!views/editor/KeywordView'
  'tpl!templates/editor/DescriptiveKeyword.tpl'
], (_, Backbone, ObjectInputView, ChildView, KeywordView, template) -> ObjectInputView.extend

  template: template

  events: ->
    _.extend {}, ObjectInputView.prototype.events,
      'click .add': -> do @add

      'click .predefined': (event) -> @addPredefined event

  initialize: (options) ->
    ObjectInputView.prototype.initialize.call @, options
    do @render
    keywordType = @model.get 'type'
    if keywordType?
      # IE only supports .startsWith() in MS Edge (> version 11)
      if keywordType.lastIndexOf('Catalogue topic', 0) == 0
        @$('#catalogueTopic').removeClass 'hidden'
        @$('.add').addClass 'hidden'
        @$('select.type').attr 'disabled', 'disabled'

    @keywords = @model.getRelatedCollection 'keywords'
    @createList @keywords, '.keywords', @addOne

    @$('input.date').datepicker dateFormat: "yy-mm-dd"

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('.type').val @model.get 'type'
    @

  addOne: (model, keywordIndex) ->
    @data = _.omit @data, 'el'
    new ChildView _.extend {}, @data,
      model: model
      keywordIndex: keywordIndex
      ObjectInputView: KeywordView

  add: -> @keywords.add {}

  addPredefined: (event) ->
    event.preventDefault()
    $target = @$(event.target)
    @keywords.add
      value: $target.text()
      uri: $target.attr 'href'

  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()

    if value
      @model.set name, value
    else
      @model.unset name
