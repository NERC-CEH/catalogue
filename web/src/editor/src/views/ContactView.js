import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'

import template from '../templates/Contact'
import { createOrgAutocomplete } from '../utils'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    this.hasRoleDefault = options.roleDefault !== undefined
    this.roleDefault = options.roleDefault || 'pointOfContact'
    this.optionTemplate = _.template('<option value="<%= value %>"><%= label %></option>')
    this.options = Array.isArray(options.roleoptions)
      ? options.roleoptions
      : [
          { value: 'author', label: 'Author' },
          { value: 'custodian', label: 'Custodian' },
          { value: 'distributor', label: 'Distributor' },
          { value: 'originator', label: 'Originator' },
          { value: 'owner', label: 'Owner' },
          { value: 'pointOfContact', label: 'Point of contact' },
          { value: 'principalInvestigator', label: 'Principal investigator' },
          { value: 'processor', label: 'Processor' },
          { value: 'publisher', label: 'Publisher' },
          { value: 'resourceProvider', label: 'Resource provider' },
          { value: 'user', label: 'User' }
        ]
    ObjectInputView.prototype.initialize.apply(this)
    createOrgAutocomplete('.orgAutocomplete', this)

    if (!this.model.get('role')) {
      this.model.set('role', this.roleDefault)
    }
  },

  render () {
    ObjectInputView.prototype.render.apply(this)

    if (this.options.length > 0) {
      this.options.forEach(option => {
        this.$('.role-select').append(this.optionTemplate(option))
      })
      this.$('.role-select').val(this.roleDefault)
    }

    if (this.hasRoleDefault) {
      this.$('.role').addClass('d-none')
    }

    if (this.roleDefault !== 'contributor') {
      this.$('.contributorRole').addClass('d-none')
    }

    this.$('select.role-select').val(this.model.get('role'))
    this.$('select.contributorRole-select').val(this.model.get('contributorRole'))
    this.$('select.honorificPrefix').val(this.model.get('honorificPrefix'))

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

    const selectedRole = this.$('.role-select').val()
    this.model.set('role', selectedRole || this.roleDefault)
  }
})
