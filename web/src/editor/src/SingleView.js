import Backbone from 'backbone'
import singleTemplate from './singleTemplate'

export default Backbone.View.extend({

  className: 'component',

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
    this.$el.html(singleTemplate({ data: this.data }))
    return this
  },

  updateMetadataModel (attribute) {
    this.model.set(this.data.modelAttribute, attribute)
  }
})
