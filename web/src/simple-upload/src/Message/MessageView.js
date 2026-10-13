import Backbone from 'backbone'
import _ from 'underscore'

const template = _.template('<span class="<%- type %>"><%- message %></span>')
export default Backbone.View.extend({

  tagName: 'li',

  initialize () {
    this.template = template
    this.listenTo(this.model, 'remove', this.remove)
  },

  render () {
    // Fill in both fields: the template reads them as variables, so a missing one throws
    this.$el.html(this.template(_.extend({ type: 'info', message: '' }, this.model.attributes)))
    return this
  }
})
