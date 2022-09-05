import Backbone from 'backbone'
import _ from 'underscore'
import template from './Message.tpl'
export default Backbone.View.extend({

  tagName: 'li',

  initialize () {
    this.template = _.template(template)
    this.listenTo(this.model, 'remove', this.remove)
  },

  render () {
    this.$el.html(this.template(this.model.attributes))
    return this
  }
})
