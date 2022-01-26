import Backbone from 'backbone'
import {PermissionView} from "./PermissionView";

export var PermissionAppView = Backbone.View.extend({

  el: '.permission',

  initialize() {
    return this.listenTo(this.model, 'loaded', this.render);
  },

  render() {
    const view = new PermissionView({model: this.model.getPermission()});
    view.render();
    return this;
  }
});