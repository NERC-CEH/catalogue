define [
  'underscore'
  'cs!views/editor/SingleView'
  'tpl!templates/editor/Parent.tpl'
  'tpl!templates/editor/MultiString.tpl'
], (_, SingleView, parentTemplate, childTemplate) -> SingleView.extend

  childTemplate: childTemplate

  events:
    change: 'modify'
    'click .remove': 'removeChild'
    'click .add': 'addChild'

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    @array = if @model.has @data.modelAttribute then _.clone @model.get @data.modelAttribute else []
    do @render

    @$(".existing").sortable
      start: (event, ui) =>
        @_oldPosition = ui.item.index()
      update: (event, ui) =>
        toMove = (@array.splice @_oldPosition, 1)[0]
        @array.splice ui.item.index(), 0, toMove
        do @updateModel

  renderParent: ->
        @$el.html parentTemplate data: @data

  render: ->
    do @renderParent
    $attach = @$(".existing")
    _.each @array, (string, index) =>
      $attach.append @childTemplate data: _.extend {}, @data,
        index: index
        value: string
    @

  modify: (event) ->
    $target = $(event.target)
    index = $target.data('index')
    value = $target.val()
    @array.splice index, 1, value
    do @updateModel

  removeChild: (event) ->
    do event.preventDefault
    $target = $(event.currentTarget)
    index = $target.data('index')
    @array.splice index, 1
    @$("#input#{ @data.modelAttribute}#{ index }").remove()
    do @updateModel

  addChild: (event) ->
    do event.preventDefault
    @array.push ""
    index = @array.length - 1
    @$(".existing").append @childTemplate data: _.extend {}, @data,
      index: index
    @$("#input#{ @data.modelAttribute}#{ index } input").focus()
    do @updateModel

  updateModel: ->
    @model.set @data.modelAttribute, _.clone @array
