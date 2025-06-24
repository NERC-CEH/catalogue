import Backbone from 'backbone'
import _ from 'underscore'
import $ from 'jquery'
import dataResourceTemplate from '../templates/DataResource'

export default Backbone.View.extend({
  events: {
    'change input, textarea, select': 'onFieldChange'
  },

  initialize () {
    this.template = dataResourceTemplate
    this.continueValidate = false
    this.render()
  },

  onFieldChange (e) {
    e.preventDefault()
    e.stopPropagation()
    const $target = $(e.target)
    const value = $target.attr('type') === 'checkbox' ? $target.prop('checked') : $target.val().trim()
    const name = $target.data('name')

    this.model.set(name, value)

    if (name === 'resourceType' || name === 'resourceFormat') {
      const nameOther = name + 'Other'
      const otherItem = this.$(`[data-name="${nameOther}"]`)
      if (value === 'Other') {
        otherItem.show()
      } else {
        otherItem.hide()
        otherItem.val('')
        this.model.set(nameOther, '')
      }
    }

    if (name === 'resourceType') {
      const easilyRecreated = this.$('.easilyRecreated')
      if (value === 'Other' || value === 'Model output') {
        easilyRecreated.show()
      } else {
        easilyRecreated.hide()
        easilyRecreated.val('')
        this.model.set('easilyRecreated', '')
      }
    }

    if (this.continueValidate) {
      this.validate()
    }
  },

  render () {
    this.$el.html(this.template({ data: _.extend({}, this.data, this.model.attributes) }))
    return this
  },

  validate () {
    this.$('.is-invalid').removeClass('is-invalid')

    const validateResult = this.model.validate()
    let errorFocus
    if (validateResult && validateResult.length > 0) {
      this.continueValidate = true
      errorFocus = this.$(`[data-name="${validateResult[0].name}"]`)
      validateResult.forEach(item => {
        const target = this.$(`[data-name="${item.name}"]`)
        target.addClass('is-invalid')
        target.siblings('.invalid-feedback').text(item.message)
      })
    }

    return errorFocus
  }
})
