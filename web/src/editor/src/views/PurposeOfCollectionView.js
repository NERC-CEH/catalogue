import ObjectInputView from './ObjectInputView'
import template from '../templates/PurposeOfCollection'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
    this.listenTo(this.model, 'change:purposeOfCollection', function (model, value) {
      this.model.set('purposeOfCollection', value.value)
    })
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('value'))
    return this
  }
})
