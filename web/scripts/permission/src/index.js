import { PermissionApp, PermissionAppView } from './PermissionApp'
import $ from 'jquery'

$(document).ready(function () {
  const app = new PermissionApp()
  app.loadPermission($('#metadata').data('id'))
  const view = new PermissionAppView({ model: app })
})
