import Backbone from 'backbone'
import _ from 'underscore'
import template from './Single.tpl'

export var SingleView = Backbone.View.extend({

  className: 'component',

  initialize (options) {
    this.template = _.template(template)
    this.data = options
    if (!this.data.ModelType) {
      this.data.ModelType = Backbone.Model
    }
  },

  show () {
    return this.$el.addClass('visible')
  },

  hide () {
    return this.$el.removeClass('visible')
  },

  render () {
    this.$el.html(this.template({ data: this.data }))
    return this
  },

  updateMetadataModel (attribute) {
    return this.model.set(this.data.modelAttribute, attribute)
  }
})
