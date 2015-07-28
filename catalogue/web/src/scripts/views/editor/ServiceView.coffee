define [
  'underscore'
  'cs!views/editor/SingleView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/ServiceTypeView'
  'cs!models/editor/Service'
  'cs!views/editor/ParentView'
  'cs!views/editor/ServiceOperationView'
  'cs!views/editor/ParentStringView'
], (_, SingleView, SingleObjectView, ServiceTypeView, Service, ParentView, ServiceOperationView, ParentStringView) -> SingleView.extend

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    service = new Service @model.get 'service'
    do @render

    @listenTo service, 'change', (model) ->
      console.log "service update #{JSON.stringify model.toJSON()}"
      @updateMetadataModel model.toJSON()

    typeView = new ServiceTypeView _.extend {}, @data,
      model: service
      modelAttribute: 'type'

    serviceOperations = new ParentView _.extend {}, @data,
      model: service
      modelAttribute: 'containsOperations'
      ObjectInputView: ServiceOperationView
      label: 'Operations'
      multiline: true
      helpText: """
                <p>Service Operations</p>
                """

    versions = new ParentStringView _.extend {}, @data,
      model: service
      modelAttribute: 'versions'
      label: 'Versions'
      helpText: """
                <p>Service Version</p>
                """

    _.invoke [serviceOperations, versions], 'show'
    @$('.dataentry').append typeView.el, serviceOperations.el, versions.el