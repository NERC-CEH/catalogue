import $ from 'jquery'
import 'bootstrap'
import { Catalogue, CatalogueView } from './CatalogueApp'

$('.catalogue-control').on('click', function (event) {
  event.preventDefault()
  $.getJSON($(event.target).attr('href'), function (data) {
    const model = new Catalogue(data)
    $.getJSON('/catalogues', function (data) {
      $(document).ready(function () {
        model.options = data.map(val => ({ value: val.id, label: val.title }))
        // eslint-disable-next-line no-unused-vars
        const view = new CatalogueView({
          el: '#metadata',
          model
        })
      })
    })
  })
})
