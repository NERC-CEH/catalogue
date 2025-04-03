import _ from 'underscore'
import $ from 'jquery'
import { ObjectInputView } from '../index'
import template from '../../templates/service-agreement/rightsHolder'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this)
    this.$('.orgAutocomplete').autocomplete({
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
        this.model.set('organisationName', ui.item.label)
        this.model.set('organisationIdentifier', ui.item.url)//
        this.$('[data-name=organisationIdentifier]').val(ui.item.url)
      }
    })
  },

  modify (event) {
    ObjectInputView.prototype.modify.call(this, event)
    this.model.set('role', 'rightsHolder')
  }
})
