/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'cs!views/editor/SingleView',
  'cs!views/editor/ServiceTypeView',
  'cs!models/editor/Service',
  'cs!views/editor/ParentView',
  'cs!views/editor/ServiceOperationView',
  'cs!views/editor/ParentStringView',
  'cs!views/editor/CoupledResourceView'
], function(_, SingleView, ServiceTypeView, Service, ParentView, ServiceOperationView, ParentStringView, CoupledResourceView) { return SingleView.extend({

  initialize(options) {
    SingleView.prototype.initialize.call(this, options);
    const service = new Service(this.model.get('service'));
    (this.render)();

    this.listenTo(service, 'change', function(model) {
      return this.updateMetadataModel(model.toJSON());
    });

    const typeView = new ServiceTypeView({
      model: service,
      modelAttribute: 'type',
      disabled: options.disabled
    });

    const serviceOperations = new ParentView({
      model: service,
      modelAttribute: 'containsOperations',
      ObjectInputView: ServiceOperationView,
      label: 'Operations',
      multiline: true,
      disabled: options.disabled
    });

    const coupledResources = new ParentView({
      model: service,
      modelAttribute: 'coupledResources',
      ObjectInputView: CoupledResourceView,
      label: 'Coupled Resources',
      multiline: true,
      disabled: options.disabled
    });

    const versions = new ParentStringView({
      model: service,
      modelAttribute: 'versions',
      label: 'Versions',
      disabled: options.disabled
    });

    _.invoke([serviceOperations, coupledResources, versions], 'show');
    return this.$('.dataentry').append(typeView.el, serviceOperations.el, coupledResources.el, versions.el);
  }
});
 });