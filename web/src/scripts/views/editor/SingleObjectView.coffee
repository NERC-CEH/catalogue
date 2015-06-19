define [
  'underscore'
  'cs!views/editor/SingleView'
], (_, SingleView) -> SingleView.extend

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    do @render

    inputModel = new @data.ModelType @model.get @data.modelAttribute
    @listenTo inputModel, 'change', @updateMetadataModel

    new @data.ObjectInputView _.extend {}, @data,
      el: @$('.dataentry')
      model: inputModel

  updateMetadataModel: (model) ->
    @model.set @data.modelAttribute, model