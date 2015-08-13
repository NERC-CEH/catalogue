define [
  'underscore'
  'backbone'
  'cs!collections/Positionable'
  'cs!views/editor/ObjectInputView'
  'cs!views/editor/ChildView'
  'cs!views/editor/KeywordView'
  'tpl!templates/editor/DescriptiveKeyword.tpl'
], (_, Backbone, Positionable, ObjectInputView, ChildView, KeywordView, template) -> ObjectInputView.extend

  template: template

  events: ->
    _.extend {}, ObjectInputView.prototype.events,
      'click .descriptiveKeywordType button': (event) ->
        @$('.descriptiveKeywordType button').addClass('btn-default').removeClass('btn-primary')
        @model.set 'type', @$(event.target).toggleClass('btn-primary').text().toLowerCase()

      'click .dateType button': (event) ->
        @$('.dateType button').addClass('btn-default').removeClass('btn-primary')
        dateType = @$(event.target).toggleClass('btn-primary').text().toLowerCase()
        thesaurusName = _.clone @model.get 'thesaurusName'
        @model.set 'thesaurusName', _.extend thesaurusName,
          dateType: dateType

      'click .add': -> do @add

      'click .predefined': -> @addPredefined event

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
    @$attach = @$('.keywords')

    @$attach.sortable
      start: (event, ui) =>
        @_oldPosition = ui.item.index()
      update: (event, ui) =>
        @keywords.position @_oldPosition, ui.item.index()

    @keywords = new Positionable []

    @listenTo @keywords, 'add', @addOne
    @listenTo @keywords, 'reset', @addAll
    @listenTo @keywords, 'add remove change position', @updateModel

    @keywords.reset @model.get 'keywords'

  render: ->
    ObjectInputView.prototype.render.apply @
    type = @model.get 'type'
    @$('.descriptiveKeywordType button').each ->
      $this = $ @
      if $this.text().toLowerCase() == type
        $this.toggleClass 'btn-default btn-primary'

    @$('input.date').datepicker dateFormat: "yy-mm-dd"

    dateType = @model.get('thesaurusName')?.dateType
    @$('.dateType button').each ->
      $this = $ @
      if $this.text().toLowerCase() == dateType
        $this.toggleClass 'btn-default btn-primary'
    @

  addOne: (model, keywordIndex) ->
    @data = _.omit @data, 'el'
    view = new ChildView _.extend {}, @data,
      model: model
      keywordIndex: keywordIndex
      ObjectInputView: KeywordView
    @$attach.append view.el

  addAll: ->
    @$attach.html('')
    @keywords.each @addOne, @

  add: ->
    @keywords.add new Backbone.Model()

  addPredefined: (event) ->
    event.preventDefault()
    $target = @$(event.target)
    @keywords.add new Backbone.Model
      value: $target.text()
      uri: $target.attr 'href'

  updateModel: ->
    @model.set 'keywords', @keywords.toJSON()

  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()

    if _.contains(['title', 'date'], name)
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
