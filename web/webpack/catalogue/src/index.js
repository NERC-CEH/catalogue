import 'bootstrap'
import $ from 'jquery'
import { Catalogue, CatalogueView } from './CatalogueApp'

console.log('loaded catalogue app') // app does not load without this line
$('.catalogue-control').on('click', function (event) {
  event.preventDefault()
  $.getJSON($(event.target).attr('href'), function (data) {
    const model = new Catalogue(data)
    $.getJSON('/catalogues', function (data) {
      $(document).ready(function () {
        model.options = data.map(val => ({ value: val.id, label: val.title }))
        const view = new CatalogueView({
          el: '#metadata',
          model
        })
      })
    })
  })
})
