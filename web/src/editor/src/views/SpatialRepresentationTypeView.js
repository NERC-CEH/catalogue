import _ from 'underscore'
import ParentStringView from './ParentStringView'
import childTemplate from '../templates/SpatialRepresentationType'
import template from '../templates/ObservationCapability'

export default ParentStringView.extend({
  initialize () {
    this.template = template
    this.childTemplate = childTemplate
  },
  render () {
    ParentStringView.prototype.render.apply(this)
    _.each(this.array, (string, index) => {
      this.$(`#input${this.data.modelAttribute}${index} select`).val(string)
    })
    return this
  }
})
