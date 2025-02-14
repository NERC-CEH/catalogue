import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'

import template from '../templates/Contact'

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
        this.model.set('organisationIdentifier', ui.item.url)
        this.$('[data-name=organisationIdentifier]').val(ui.item.url)
      }
    })
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select.role').val(this.model.get('role'))
    return this
  },

  modify (event) {
    const $target = $(event.target)
    const name = $target.data('name')
    const value = $target.val()

    if (_.contains(['deliveryPoint', 'city', 'administrativeArea', 'country', 'postalCode'], name)) {
      let address = _.clone(this.model.get('address'))
      if (value) {
        address[name] = value
        this.model.set('address', address)
      } else {
        address = _.omit(address, name)
        this.model.set('address', address)
      }
    } else {
      if (value) {
        this.model.set(name, value)
      } else {
        this.model.unset(name)
      }
    }
  }
})
