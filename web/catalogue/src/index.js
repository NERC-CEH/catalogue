import { CatalogueApp, CatalogueView } from './CatalogueApp'
import $ from 'jquery'

$(document).ready(function () {
  const app = new CatalogueApp()
  app.loadPermission($('#metadata').data('id'))
  const view = new CatalogueView({ model: app })
})
