import _ from 'underscore'
import ParentStringView from './ParentStringView'
import childTemplate from '../templates/SpatialRepresentationType'

export default ParentStringView.extend({

  initialize (options) {
    this.childTemplate = childTemplate
    ParentStringView.prototype.initialize.call(this, options)
  },

  render () {
    ParentStringView.prototype.render.apply(this)
    _.each(this.array, (string, index) => {
      this.$(`#input${this.data.modelAttribute}${index} select`).val(string)
    })
    return this
  }
})
