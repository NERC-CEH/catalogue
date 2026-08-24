import _ from 'underscore'
import $ from 'jquery'

export function createOrgAutocomplete( id, obj, identifierFieldName = 'organisationIdentifier') {
  const selector = `[data-name=${identifierFieldName}]`

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

      $.getJSON(query, data =>
        response(
          _.map(data, d => ({
            label: d.name,
            url: d.id
          }))
        )
      )
    },
    select: (event, ui) => {
      obj.model.set('organisationName', ui.item.label)
      obj.model.set(identifierFieldName, ui.item.url)

      const $identifierField = obj.$(selector)

      if ($identifierField.length) {
        $identifierField.val(ui.item.url)
      }
    }
  })

  obj.$(id).on('input', () => {
    obj.model.set(identifierFieldName, '')

    const $identifierField = obj.$(selector)

    if ($identifierField.length) {
      $identifierField.val('')
    }
  })
}
