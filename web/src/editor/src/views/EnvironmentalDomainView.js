import ObjectInputView from './ObjectInputView'
import template from '../templates/EnvironmentalDomain'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
    this.listenTo(this.model, 'change:environmentalDomain', function (model, value) {
      this.model.set('environmentalDomain', value.value)
    })
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('value'))
    return this
  }
})
