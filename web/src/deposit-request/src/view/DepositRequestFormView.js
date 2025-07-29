import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from '../../../editor/src/views/ObjectInputView'
import DataResourceView from './DataResourceView'
import ChildView from '../../../editor/src/views/ChildView'
import DataResource from '../model/DataResource'
import depositRequestFormTemplate from '../templates/DepositRequestForm'

export default ObjectInputView.extend({
  events: function () {
    return _.extend({}, ObjectInputView.prototype.events, {
      'change input, textarea, select': 'onFieldChange',
      'click .add-resource': 'addResource',
      'click .btn-submit': 'onSubmit'
    })
  },

  initialize (options) {
    this.template = depositRequestFormTemplate
    this.dataResourceViewList = []
    ObjectInputView.prototype.initialize.call(this, options)
    this.dataResourceList = this.model.getDataResources()
    this.createList(this.dataResourceList, '.resource-list', this.newDataResource, true)
    this.continueValidate = false

    this.listenTo(this.dataResourceList, 'remove', this.onDataResourceRemoved)
  },

  render () {
    this.$el.html(this.template({
      model: this.model
    }))
    return this
  },

  addResource (e) {
    e.preventDefault()
    this.dataResourceList.add(new DataResource())
    this.updateAddButtonLabel()
  },

  newDataResource (model, i) {
    const view = new ChildView({
      model,
      index: i,
      ObjectInputView: DataResourceView
    })
    this.dataResourceViewList.push(view)

    const target = this.$('[data-name="dataResources"]')
    target.removeClass('is-invalid')
    target.siblings('.invalid-feedback').text('')

    return view
  },

  onDataResourceRemoved (model) {
    const index = this.dataResourceViewList.findIndex(view => view.model === model)
    if (index !== -1) {
      this.dataResourceViewList.splice(index, 1)
    }
    this.updateAddButtonLabel()
  },

  onFieldChange (e) {
    e.preventDefault()
    e.stopPropagation()
    const $target = $(e.target)
    const value = $target.attr('type') === 'checkbox' ? $target.prop('checked') : $target.val().trim()
    const name = $target.data('name')

    this.model.set(name, value)

    if (name === 'funder') {
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

    if (this.continueValidate) {
      this.validate()
    }
  },

  updateAddButtonLabel () {
    const label = this.dataResourceList.length === 0 ? 'Add a dataset' : 'Add another dataset'
    this.$('.add-resource').text(label)
  },

  validate () {
    this.$('.is-invalid').removeClass('is-invalid')

    let errorFocus
    const validateResult = this.model.validate()
    if (validateResult && validateResult.length > 0) {
      this.continueValidate = true
      errorFocus = this.$(`[data-name="${validateResult[0].name}"]`)
      validateResult.forEach(item => {
        const target = this.$(`[data-name="${item.name}"]`)
        target.addClass('is-invalid')
        target.siblings('.invalid-feedback').text(item.message)
      })
    }

    this.dataResourceViewList.forEach(item => {
      const result = item.contentView.validate()
      if (!errorFocus && result) {
        errorFocus = result
      }
    })

    return errorFocus
  },

  onSubmit (e) {
    e.preventDefault()

    const errorFocus = this.validate()
    if (errorFocus) {
      errorFocus[0].scrollIntoView({ behavior: 'smooth', block: 'center' })
      return
    }

    const submitBtn = this.$('.btn-submit')
    const originalText = submitBtn.html()
    submitBtn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Submitting...')
    this.$('#message-box').html('').hide()

    const modelData = this.model.toJSON()
    this.model.save(modelData, {
      wait: true,
      contentType: 'application/json',
      dataType: 'text',
      data: JSON.stringify(modelData),
      success: (model, response, options) => {
        this.onSuccess(response, options.xhr)
      },
      error: (model, xhr) => {
        this.onError(xhr)
      },
      complete: () => {
        submitBtn.prop('disabled', false).html(originalText)
      }
    })
  },

  onSuccess (response, xhr) {
    const location = xhr && xhr.getResponseHeader
      ? xhr.getResponseHeader('Location')
      : null
    if (location) {
      window.location.href = location
      return
    }

    const msg = typeof response === 'string' && response.trim() !== ''
      ? response
      : 'Submission successful.'

    const target = this.$('#message-box')
    target.html(msg).show()
    target[0].scrollIntoView({ behavior: 'smooth', block: 'center' })
  },

  onError (xhr) {
    let msg = xhr?.responseText?.trim()

    if (!msg) {
      msg = `Status: ${xhr.status} - ${xhr.statusText || 'An unknown error occurred'}`
    }
    const target = this.$('#message-box')
    target.html(msg).show()
    target[0].scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
})
