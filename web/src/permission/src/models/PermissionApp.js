import Backbone from 'backbone'
import _ from 'underscore'
import {Permission} from "../models/Permission";

export var PermissionApp = Backbone.Model.extend({

  loadPermission(identifier) {
    const permission = new Permission({
      id: identifier});

    return permission.fetch({
      success: model => {
        (model.loadCollection)();
        this.set('permission', model);
        return this.trigger('loaded');
      },

      error: model => {
        return this.trigger('error', `Unable to load permission for: ${model.id}`);
      }
    });
  },

  getPermission() {
    return _.clone(this.get('permission'));
  }
});