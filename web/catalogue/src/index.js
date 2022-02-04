import $ from 'jquery'
import { Catalogue, CatalogueView } from './CatalogueApp'

$(document).ready(function () {
  $('.catalogue-control').on('click', function (event) {
    event.preventDefault()
    $.getJSON($(event.target).attr('href'), function (data) {
      const model = new Catalogue(data)
      $.getJSON('/catalogues', function (data) {
        model.options = data.map(val => ({ value: val.id, label: val.title }))
        const view = new CatalogueView({
          el: '#metadata',
          model
        })
        view.render()
      })
    })
  })
})
