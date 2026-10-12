import Backbone from 'backbone'
import template from './singleTemplate'

export default Backbone.View.extend({

  className: 'component',

  // Whether the view renders a control with id input-<modelAttribute> for the section label to name.
  // Repeating and compound sections have no single control, so their label must not point at one.
  labelsControl: false,

  initialize (options) {
    this.data = options
    if (!this.data.ModelType) {
      this.data.ModelType = Backbone.Model
    }
  },

  show () {
    this.$el.addClass('visible')
  },

  hide () {
    this.$el.removeClass('visible')
  },

  render () {
    this.$el.html(template({ data: this.data, labelsControl: this.labelsControl }))
    return this
  },

  updateMetadataModel (attribute) {
    this.model.set(this.data.modelAttribute, attribute)
  }
})
