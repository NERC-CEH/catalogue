import _ from 'underscore'
import $ from 'jquery'
import { autocomplete } from './autocomplete'

export function createOrgAutocomplete (id, obj, identifierFieldName = 'organisationIdentifier', nameFieldName = 'organisationName') {
  const selector = `[data-name=${identifierFieldName}]`

  obj.$(id).each((i, input) => autocomplete(input, {
    minLength: 2,
    source: async term => {
      term = term.trim()
      const query = _.isEmpty(term) ? '/organisation/names' : `/organisation/names?${$.param({ query: term })}`
      try {
        const data = await $.getJSON(query)
        return _.map(data, d => ({ label: d.name, url: d.id }))
      } catch {
        return []
      }
    },
    select: item => {
      obj.model.set(nameFieldName, item.label)
      obj.model.set(identifierFieldName, item.url)

      const $identifierField = obj.$(selector)

      if ($identifierField.length) {
        $identifierField.val(item.url)
      }
    }
  }))

  obj.$(id).on('input', () => {
    obj.model.set(identifierFieldName, '')

    const $identifierField = obj.$(selector)

    if ($identifierField.length) {
      $identifierField.val('')
    }
  })
}
