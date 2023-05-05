import Backbone from 'backbone'
import _ from 'underscore'
import template from './Single.tpl'

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
    this.singleTemplate = _.template(template)
    this.$el.html(this.singleTemplate({ data: this.data }))
    return this
  },

  updateMetadataModel (attribute) {
    this.model.set(this.data.modelAttribute, attribute)
  }
})
