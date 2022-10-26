import Backbone from 'backbone'
import template from './IdentityPermission.tpl'
import _ from 'underscore'

export default Backbone.View.extend({
  tagName: 'tr',

  events: {
    'click button': 'removePermission',
    'click [type="checkbox"]': 'update'
  },

  initialize () {
    this.template = _.template(template)
  },

  removePermission () {
    this.model.parent.removePermission(this.model)
  },

  update (event) {
    const permission = $(event.target).data('permission')
    this.model.set(permission, !this.model.get(permission))
  },

  render () {
    this.$el.html(this.template(this.model.attributes))
    return this
  }
})
