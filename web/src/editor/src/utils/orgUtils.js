import _ from 'underscore'
import $ from 'jquery'

export function createOrgAutocomplete (id, obj) {
  obj.$(id).autocomplete({
    minLength: 2,
    source: (request, response) => {
      let query
      const term = request.term.trim()
      if (_.isEmpty(term)) {
        query = '/organisation/names'
      } else {
        query = `/organisation/names?query=${request.term}`
      }
      $.getJSON(query, data => response(_.map(data, d => ({
        label: d.name,
        url: d.id
      }))))
    },
    select: (event, ui) => {
      obj.model.set('organisationName', ui.item.label)
      obj.model.set('organisationIdentifier', ui.item.url)
      if (obj.$('[data-name=organisationIdentifier]').length) {
        obj.$('[data-name=organisationIdentifier]').val(ui.item.url)
      }
    }
  })
  ;

  // Add event listener for changes to the organisationName field
  obj.$(id).on('input', () => {
    obj.model.set('organisationIdentifier', '');
    if (obj.$('[data-name=organisationIdentifier]').length) {
      obj.$('[data-name=organisationIdentifier]').val('');
    }
  });

}
