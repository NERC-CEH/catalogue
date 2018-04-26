define [
  'underscore'
  'cs!views/editor/SingleView'
  'cs!views/editor/ServiceTypeView'
  'cs!models/editor/Service'
  'cs!views/editor/ParentView'
  'cs!views/editor/ServiceOperationView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/CoupledResourceView'
], (_, SingleView, ServiceTypeView, Service, ParentView, ServiceOperationView, ParentStringView, CoupledResourceView) -> SingleView.extend

  initialize: (options) ->
    SingleView.prototype.initialize.call @, options
    service = new Service @model.get 'service'
    do @render

    @listenTo service, 'change', (model) ->
      @updateMetadataModel model.toJSON()

    typeView = new ServiceTypeView
      model: service
      modelAttribute: 'type'
      disabled: options.disabled

    serviceOperations = new ParentView
      model: service
      modelAttribute: 'containsOperations'
      ObjectInputView: ServiceOperationView
      label: 'Operations'
      multiline: true
      disabled: options.disabled

    coupledResources = new ParentView
      model: service
      modelAttribute: 'coupledResources'
      ObjectInputView: CoupledResourceView
      label: 'Coupled Resources'
      multiline: true
      disabled: options.disabled

    versions = new ParentStringView
      model: service
      modelAttribute: 'versions'
      label: 'Versions'
      disabled: options.disabled

    _.invoke [serviceOperations, coupledResources, versions], 'show'
    @$('.dataentry').append typeView.el, serviceOperations.el, coupledResources.el, versions.el