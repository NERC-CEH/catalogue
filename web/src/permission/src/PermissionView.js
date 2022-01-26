import Backbone from 'backbone'
import _ from 'underscore'
import tpl from "./Permissions.tpl";
import {IdentityPermissionView} from "./IdentityPermissionView";
import {IdentityPermission} from "./IdentityPermission";

export var PermissionView = Backbone.View.extend({
  el: '.permission',

  events: {
    'click #permissionSave': 'save',
    'click #permissionAdd': 'add'
  },

  initialize() {
    this.listenTo(this.model, 'sync', this.reload);
    this.listenTo(this.model, 'permission:add', this.addAll);
    this.listenTo(this.model, 'permission:remove', this.addAll);
    return this.listenTo(this.model, 'save:success', this.leave);
  },

  save() {
    return this.model.save({}, {
      success: () => {
        return this.model.trigger('save:success', "Save successful");
      },
      error: (model, response) => {
        return this.model.trigger('save:error', `Error saving permission: ${response.status} (${response.statusText})`);
      }
    }
    );
  },

  addOne(permission) {
    _.extend(permission, {parent: this.model});
    const view = new IdentityPermissionView({model: permission});
    return this.$('tbody').append(view.render().el);
  },

  addAll() {
    this.$('tbody').html('');
    return this.model.get('permissions').each(this.addOne, this);
  },

  render() {
    const template = _.template(tpl);
    this.$el.html(template(this.model.toJSON()));
    this.addAll();
    return this;
  },

  reload() {
    (this.model.loadCollection)();
    return (this.addAll)();
  },

  add() {
    const identity = $('#identity').val();
    if (identity) {
      const permission = new IdentityPermission({
        identity,
        canView: $('#canView').prop('checked'),
        canEdit: $('#canEdit').prop('checked'),
        canDelete: $('#canDelete').prop('checked'),
        canUpload: $('#canUpload').prop('checked')
      });
      this.model.addPermission(permission);
    }

    $('#identity').val("");
    $('#canView').prop('checked', false);
    $('#canEdit').prop('checked', false);
    $('#canDelete').prop('checked', false);
    return $('#canUpload').prop('checked', false);
  },

  leave() {
    return window.location.assign(`/documents/${this.model.get('id')}/permission`);
  }
});