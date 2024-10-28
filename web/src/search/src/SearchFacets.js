import $ from 'jquery'

export function createFacetSearch (facets) {
  facets.forEach(facet => {
    const id = '#search-facet-' + facet.displayName.replaceAll(' ', '-')
    if (typeof facet.results !== 'undefined') {
      const data = facet.results
        .filter(item => !item.active)
        .map(item => ({
          label: item.name,
          url: item.url
        }))
      $(id).autocomplete({
        minLength: 1,
        source: data,
        appendTo: '#search',
        select: (event, ui) => {
          $(id).val(ui.item.label)
          $('a[href="' + ui.item.url + '"]').first().trigger('click')
          return false
        }
      })
    }
  })
}
