import _ from 'underscore'
import ParentStringView from './ParentStringView'
import childTemplate from '../templates/SpatialRepresentationType.tpl'

export default ParentStringView.extend({
  render () {
    this.childTemplate = _.template(childTemplate)
    ParentStringView.prototype.render.apply(this)
    return _.each(this.array, (string, index) => {
      return this.$(`#input${this.data.modelAttribute}${index} select`).val(string)
    })
  }
})
