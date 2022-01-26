import Backbone from 'backbone'
import tpl from "./IdentityPermission.tpl";
import _ from "underscore/underscore-node.mjs";

export var IdentityPermissionView = Backbone.View.extend({
  tagName: 'tr',

  events: {
    'click button': 'removePermission',
    'click [type="checkbox"]': 'update'
  },

  removePermission() {
    return this.model.parent.removePermission(this.model);
  },

  update(event) {
    const permission = $(event.target).data('permission');
    return this.model.set(permission, !this.model.get(permission));
  },

  render() {
    const template = _.template(tpl);
    this.$el.html(template(this.model.toJSON()));
    return this;
  }
});