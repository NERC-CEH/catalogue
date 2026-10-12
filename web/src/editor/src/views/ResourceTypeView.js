import ObjectInputView from './ObjectInputView'
import template from '../templates/ResourceType'

export default ObjectInputView.extend({

  // Renders the select#input-<modelAttribute> that the section label names
  labelsControl: true,

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('value'))
    return this
  }
})
