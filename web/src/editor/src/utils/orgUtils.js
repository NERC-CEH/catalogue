import _ from 'underscore'
import $ from 'jquery'

export function createOrgAutocomplete (id, obj, identifierFieldName = 'organisationIdentifier', nameFieldName = 'organisationName') {
  const selector = `[data-name=${identifierFieldName}]`

  obj.$(id).autocomplete({
    minLength: 2,
    source: (request, response) => {
      const term = request.term.trim()
      const query = _.isEmpty(term) ? '/organisation/names' : `/organisation/names?${$.param({ query: term })}`

      $.getJSON(query)
        .done(data =>
          response(
            _.map(data, d => ({
              label: d.name,
              url: d.id
            }))
          )
        )
        // Always answer, or the autocomplete stays in its loading state
        .fail(() => response([]))
    },
    select: (event, ui) => {
      obj.model.set(nameFieldName, ui.item.label)
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
