import ObjectInputView from './ObjectInputView'
import template from '../templates/FacilityType'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
    this.listenTo(this.model, 'change:facilityType', function (model, value) {
      this.model.set('facilityType', value.value)
    })
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('value'))
    return this
  }
})
