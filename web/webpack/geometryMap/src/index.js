import $ from 'jquery'
import 'bootstrap'
import BoundingBoxView from './BoundingBoxView'

$(document).ready(function () {
  const view = new BoundingBoxView({ el: '#geometry-map' })
})
