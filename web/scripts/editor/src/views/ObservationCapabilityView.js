import ObjectInputViewForObjects from './ObjectInputViewForObjects'
import _ from 'underscore'
import template from '../templates/ObservationCapability.tpl'

export default ObjectInputViewForObjects.extend({

  render () {
    this.template = _.template(template)
    if (!Array.from(this.data).includes('observedPropertyName')) {
      this.data.observedPropertyName = {}
    }

    if (!Array.from(this.data).includes('observedPropertyUnitOfMeasure')) {
      this.data.observedPropertyUnitOfMeasure = {}
    }

    if (!Array.from(this.data).includes('procedureName')) {
      this.data.procedureName = {}
    }

    ObjectInputViewForObjects.prototype.render.apply(this)
    return this
  }
})
