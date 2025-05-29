import { NestedModel } from './NestedModel'
import ObservedProperty from './ObservedProperty'

export default NestedModel.extend({
  defaults: {
    filesetName: '',
    encodingFormat: '',
    includes: '',
    observedProperty: []
  },

  initialize () {
    NestedModel.prototype.initialize.apply(this, arguments)
  },

  getObservedProperty () {
    return this.getRelatedCollection('observedProperty', ObservedProperty)
  }
})
