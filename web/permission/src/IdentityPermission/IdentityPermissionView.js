import Backbone from 'backbone'
import template from './IdentityPermission.tpl'
import _ from 'underscore/underscore-node.mjs'

export var IdentityPermissionView = Backbone.View.extend({
  tagName: 'tr',

  events: {
    'click button': 'removePermission',
    'click [type="checkbox"]': 'update'
  },

  initialize () {
    this.model.set(_.template(template))
  },

  removePermission () {
    return this.model.parent.removePermission(this.model)
  },

  update (event) {
    const permission = $(event.target).data('permission')
    return this.model.set(permission, !this.model.get(permission))
  },

  render () {
    this.$el.html(this.model.attributes())
    return this
  }
})
