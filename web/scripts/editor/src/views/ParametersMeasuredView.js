import _ from 'underscore'
import template from '../templates/ParametersMeasured.tpl'
import ObjectInputViewForObjects from './ObjectInputViewForObjects'

export default ObjectInputViewForObjects.extend({
  render () {
    this.template = _.template(template)
    if (!Array.from(this.data).includes('name')) {
      this.data.name = {}
    }

    if (!Array.from(this.data).includes('unitOfMeasure')) {
      this.data.unitOfMeasure = {}
    }

    ObjectInputViewForObjects.prototype.render.apply(this)
    return this
  }
})
