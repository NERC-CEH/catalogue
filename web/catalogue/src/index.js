import $ from 'jquery'
import _ from 'underscore'
import { Catalogue, CatalogueView } from './CatalogueApp'

$(document).ready(function () {
  const catalogues = $.getJSON('/catalogues', function (data) {
    return _.chain(data).map(c => ({
      value: c.id,
      label: c.title
    })).value()
  })

  console.log('catalogues')
  console.log(catalogues)

  $('.catalogue-control').on('click', function (event) {
    event.preventDefault()
    $.getJSON($(event.target).attr('href'), function (data) {
      const model = new Catalogue(data)
      model.options = catalogues

      const view = new CatalogueView({
        el: '#metadata',
        model
      })
      view.render()
    })
  })
})
