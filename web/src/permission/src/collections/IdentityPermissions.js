import Backbone from 'backbone'
import {IdentityPermission} from "../models/IdentityPermission";

export var IdentityPermissions = Backbone.Collection.extend({

  model: IdentityPermission,
  comparator: 'identity'

});