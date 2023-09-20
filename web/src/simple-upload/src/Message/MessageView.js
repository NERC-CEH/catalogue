import Backbone from 'backbone'
import _ from 'underscore'

const template = _.template('<span class="<%= type %>"><%= message %></span>')
export default Backbone.View.extend({

  tagName: 'li',

  initialize () {
    this.template = template
    this.listenTo(this.model, 'remove', this.remove)
  },

  render () {
    this.$el.html(this.template(this.model.attributes))
    return this
  }
})
