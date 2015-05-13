define [
  'backbone'
  'tpl!templates/editor/AlternateTitles.tpl'
  'cs!views/editor/AlternateTitlesItemView'
], (Backbone, template, ItemView) -> Backbone.View.extend

  events:
    'click #alternateTitleAdd': 'add'
    'keydown #alternateTitle': 'addEnter'

  initialize: ->
    if not @model
      throw new Error('model is required')

    @alternateTitles = new Backbone.Collection []

    _.each @model.get('alternateTitles'), (altTitle) =>
      @alternateTitles.add new Backbone.Model
        value: altTitle

    @listenTo @alternateTitles, 'add remove change', @updateModel

  addOne: (alternateTitle) ->
    view = new ItemView model: alternateTitle
    @$('tbody').append view.render().el

  addAll: ->
    @$('tbody').html('')
    @alternateTitles.each @addOne, @

  render: ->
    @$el.html template
    do @addAll
    return @

  addEnter: (event) ->
    if event.keyCode == 13
      do @add

  add: ->
    alternateTitle = @$('#alternateTitle').val()
    if alternateTitle
      @alternateTitles.add new Backbone.Model
        value: alternateTitle

    $('#alternateTitle').val ""

  updateModel: ->
    if @alternateTitles.length > 0
      @model.set 'alternateTitles', @alternateTitles.map (model) -> model.get 'value'
    else
      @model.unset 'alternateTitles'
    do @addAll