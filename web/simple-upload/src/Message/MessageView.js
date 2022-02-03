import Backbone from 'backbone'
import template from './Message.tpl'
export var MessageView = Backbone.View.extend({

  tagName: 'li',

  template,

  initialize () {
    return this.listenTo(this.model, 'remove', this.remove)
  },

  render () {
    this.$el.html(this.template(this.model.attributes))
    return this
  }
})
