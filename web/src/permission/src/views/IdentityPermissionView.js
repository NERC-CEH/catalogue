/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'tpl!templates/IdentityPermission.tpl'
], function(Backbone, template) { return Backbone.View.extend({
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
 });