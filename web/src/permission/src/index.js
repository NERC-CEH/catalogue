import {PermissionApp} from "./PermissionApp";
import {PermissionAppView} from "./PermissionAppView";

$( document ).ready(function() {
    const app = new PermissionApp();
    const view = new PermissionAppView({model: app});
    view.render()
    console.log( "ready!" );
});