import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    versions: [],
    coupledResources: [],
    containsOperations: []
  }
})
