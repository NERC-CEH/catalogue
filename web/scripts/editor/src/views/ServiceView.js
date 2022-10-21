import _ from 'underscore'
import SingleView from '../SingleView'
import ServiceTypeView from './ServiceTypeView'
import { Service } from '../models'
import ParentView from './ParentView'
import ServiceOperationView from './ServiceOperationView'
import ParentStringView from './ParentStringView'
import CoupledResourceView from './CoupledResourceView'

export default SingleView.extend({

  initialize (options) {
    SingleView.prototype.initialize.call(this, options)
    const service = new Service(this.model.get('service'))
    this.render()

    this.listenTo(service, 'change', function (model) {
      this.updateMetadataModel(model.toJSON())
    })

    const that = this
    $(document).ready(function () {
      const typeView = new ServiceTypeView({
        model: service,
        modelAttribute: 'type',
        disabled: options.disabled
      })

      const serviceOperations = new ParentView({
        model: service,
        modelAttribute: 'containsOperations',
        ObjectInputView: ServiceOperationView,
        label: 'Operations',
        multiline: true,
        disabled: options.disabled
      })

      const coupledResources = new ParentView({
        model: service,
        modelAttribute: 'coupledResources',
        ObjectInputView: CoupledResourceView,
        label: 'Coupled Resources',
        multiline: true,
        disabled: options.disabled
      })

      const versions = new ParentStringView({
        model: service,
        modelAttribute: 'versions',
        label: 'Versions',
        disabled: options.disabled
      })
      _.invoke([serviceOperations, coupledResources, versions], 'show')
      that.$('.dataentry').append(typeView.el, serviceOperations.el, coupledResources.el, versions.el)
    })
  }
})
