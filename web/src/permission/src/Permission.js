import Backbone from 'backbone'
import _ from 'underscore'
import {IdentityPermissions} from "./IdentityPermissions";

export var Permission = Backbone.Model.extend({

  url() {
    return (this.urlRoot)();
  },

  urlRoot() {
    return `/documents/${this.id}/permission`;
  },

  loadCollection() {
    const collection = new IdentityPermissions();
    collection.reset(this.get('permissions'));
    return this.set('permissions', collection);
  },

  addPermission(permission) {
    const collection = _.clone(this.get('permissions'));
    collection.add(permission);
    this.set('permissions', collection);
    return this.trigger("permission:add");
  },

  removePermission(permission) {
    const collection = _.clone(this.get('permissions'));
    collection.remove(permission);
    this.set('permissions', collection);
    return this.trigger("permission:remove");
  }
});