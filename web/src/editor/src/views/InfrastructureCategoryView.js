import template from '../templates/InfrastructureCategory'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({

  initialize () {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this)
    this.listenTo(this.model, 'change:infrastructureCategory', function (model, value) {
      this.model.set('type', value.value)
    })
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('value'))
    return this
  }
})
