import Backbone from 'backbone'
import template from "./IdentityPermission.tpl";

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
    this.$el.html(template(this.model.toJSON()));
    return this;
  }
});