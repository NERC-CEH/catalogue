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
    title = @model.get('thesaurusName')?.title
    if title?
      # IE only supports .startsWith() in MS Edge (> version 11)
      if title.lastIndexOf('CEH Metadata', 0) == 0
        @$('#cehTopic').removeClass 'hidden'
        @$('.add').addClass 'hidden'
      else if title.lastIndexOf('GEMET - INSPIRE themes', 0) == 0
        @$('#inspireTheme').removeClass 'hidden'
        @$('.add').addClass 'hidden'

    @keywords = @createList 'keywords', '.keywords', @addOne
    @$('input.date').datepicker dateFormat: "yy-mm-dd"

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('.type').val @model.get 'type'
    @$('.dateType').val  @model.get('thesaurusName')?.dateType
    @

  addOne: (model, keywordIndex) ->
    @data = _.omit @data, 'el'
    new ChildView _.extend {}, @data,
      model: model
      keywordIndex: keywordIndex
      ObjectInputView: KeywordView

  add: ->
    @keywords.add new Backbone.Model()

  addPredefined: (event) ->
    event.preventDefault()
    $target = @$(event.target)
    @keywords.add new Backbone.Model
      value: $target.text()
      uri: $target.attr 'href'


  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()

    if _.contains(['title', 'date', 'dateType'], name)
      thesaurus = _.clone @model.get 'thesaurusName'
      if value
        thesaurus[name] = value
        @model.set 'thesaurusName', thesaurus
      else
        thesaurus = _.omit thesaurus, name
        @model.set 'thesaurusName', thesaurus
    else
      if value
        @model.set name, value
      else
        @model.unset name
