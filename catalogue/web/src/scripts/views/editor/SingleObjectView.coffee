define [
  'underscore'
  'cs!views/editor/SingleView'
], (_, SingleView) -> SingleView.extend

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    do @render

    inputModel = new @data.ModelType @model.get @data.modelAttribute
    @listenTo inputModel, 'change', @updateMetadataModel
    @listenTo @model, 'sync', (model) ->
      inputModel.set model.get @data.modelAttribute

    new @data.ObjectInputView _.extend {}, @data,
      el: @$('.dataentry')
      model: inputModel