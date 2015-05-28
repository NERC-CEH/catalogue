define [
  'backbone'
  'underscore'
  'tpl!templates/editor/AlternateTitlesItem.tpl'
], (Backbone, _, template) -> Backbone.View.extend

  events:
    'click #alternateTitleAdd': 'addNew'
    'keydown input':            'addEnter'
    'click button.remove':      'remove'
    'change input':             'modify'

  initialize: (options) ->
    if not @model
      throw new Error('model is required')

    @add = if options.add then options.add else false

    if options.alternateTitles
      @alternateTitles = options.alternateTitles

  render: ->
    @$el.html template _.extend @model.toJSON(), add: @add
    return @

  addEnter: (event) ->
    if event.keyCode == 13
      do @addNew

  addNew: ->
    if @alternateTitles
      @alternateTitles.add @model.clone(), at: 0
      @model = new Backbone.Model value: ''
      do @render

  remove: ->
    @model.collection.remove @model

  modify: (event) ->
    @model.set 'value', $(event.target).val()