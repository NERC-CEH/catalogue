import ObjectInputView from './ObjectInputView'
import template from '../templates/AccessLimitation'

export default ObjectInputView.extend({

  // Renders the select#input-<modelAttribute> that the section label names
  labelsControl: true,

  initialize () {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this, arguments)
    this.listenTo(this.model, 'change:accessLimitation', function (model, value) {
      this.model.set('type', value.value)
    })
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('value'))
    return this
  }
})
