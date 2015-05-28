define [
  'backbone'
  'tpl!templates/editor/AlternateTitles.tpl'
  'cs!views/editor/AlternateTitlesItemView'
], (Backbone, template, ItemView) -> Backbone.View.extend

  initialize: ->
    if not @model
      throw new Error('model is required')

    @alternateTitles = new Backbone.Collection []

    _.each @model.get('alternateTitles'), (altTitle) =>
      @alternateTitles.add new Backbone.Model
        value: altTitle

    @listenTo @alternateTitles, 'add remove change', @updateModel

  addOne: (alternateTitle) ->
    view = new ItemView
      model: alternateTitle
      className: 'row'
    @$('#alternateTitles').append view.render().el

  addAll: ->
    @$('#alternateTitles').html('')
    @alternateTitles.each @addOne, @

  render: ->
    @$el.html template
    do @addAll

    alternateTitle = new ItemView
      el: @$('#alternateTitlesAddNew')
      model: new Backbone.Model value: ''
      add: true
      alternateTitles: @alternateTitles
    do alternateTitle.render

    return @

  updateModel: ->
    if @alternateTitles.length > 0
      @model.set 'alternateTitles', @alternateTitles.map (model) -> model.get 'value'
    else
      @model.unset 'alternateTitles'
    do @addAll