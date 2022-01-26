import {PermissionApp} from "./PermissionApp";
import {PermissionAppView} from "./PermissionAppView";

$( document ).ready(function() {
    const app = new PermissionApp();
    app.loadPermission($("#metadata").data("id"));
    const view = new PermissionAppView({model: app});
    view.initialize();
});