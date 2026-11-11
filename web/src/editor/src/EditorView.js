import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import template from './editorTemplate'
import { copyText } from '../../clipboard/copyText'
import { dialog } from '../../dialog/src/dialog'

export default Backbone.View.extend({

  events: {
    'click #editorDelete': 'attemptDelete',
    'click #confirmDeleteYes': 'delete',
    'click #editorExit': 'attemptExit',
    'click #editorSave': 'save',
    'click #editorBack': 'back',
    'click #editorNext': 'next',
    'click .editor-nav span': 'direct'
  },

  initialize () {
    this.saveRequired = false
    this.template = template
    this.currentStep = 1
    this.catalogue = $('html').data('catalogue')

    const that = this
    this.listenTo(this.model, 'error', function (model, response) {
      if (response && response.status === 409) {
        // The model still holds the user's unsaved edits (Backbone does not revert on error), and
        // with no explicit attrs passed to save() this is exactly the JSON body that was PUT. Show it
        // so the edits can be copied out and re-applied after reloading the latest version.
        const submitted = JSON.stringify(model.toJSON(), null, 2)
        const copyButton = $('<button type="button" class="btn btn-outline-primary btn-sm mb-2">').text('Copy changes to clipboard')
        const payload = $('<textarea readonly rows="12" class="form-control font-monospace small">').val(submitted)
        copyButton.on('click', () => {
          copyText(payload[0]).then(ok => { if (ok) copyButton.text('Copied!') })
        })
        dialog.alert({
          title: 'Edit conflict',
          icon: 'warning',
          text: 'This record was changed by another user since you opened it. Your changes below were not saved. ' +
                'Reload the record to see the latest version, then re-apply your changes.',
          body: [copyButton[0], payload[0]]
        })
        return
      }

      const message = response?.responseJSON?.message || response?.responseText || response?.statusText || 'There was a problem communicating with the server!'

      dialog.alert({
        title: `Server response: ${response.status} ${response.statusText}`,
        icon: 'error',
        text: message,
        body: $('<textarea readonly rows="10" class="form-control font-monospace small">').val(JSON.stringify(model.toJSON()))[0]
      })
    })

    this.listenTo(this.model, 'sync', function () {
      that.saveRequired = false
    })
    this.listenTo(this.model, 'change save:required', function () {
      that.saveRequired = true
    })

    this.listenTo(this.model, 'invalid', function (model, errors) {
      dialog.alert({ title: 'Validation Errors', icon: 'error', text: errors })
    })

    this.render()
    _.invoke(this.sections[0].views, 'show')
    this.sections.forEach(section => {
      this.$('.editor-nav').append($(`<span role="button" title='${section.title}'>${section.label}</span>`))
    })

    this.$('.editor-nav').find('span').first().addClass('active')
  },

  attemptDelete () {
    return dialog.confirm({
      title: 'Are you sure?',
      text: 'This deletes the record.',
      confirmText: 'Yes, delete this record',
      danger: true
    }).then((result) => {
      if (result.isConfirmed) {
        this.delete()
      }
    })
  },

  // Shown in progress in its own button, as a save is
  delete () {
    this.$('#editorDelete')
      .prop('disabled', true)
      .html('Deleting… <i class="fa-solid fa-spinner fa-spin"></i>')
    this.model.destroy({
      error: () => {
        this.$('#editorDelete')
          .prop('disabled', false)
          .html('Delete <i class="fa-solid fa-times"></i>')
      },
      success: () => {
        _.invoke(this.sections, 'remove')
        this.remove()
        Backbone.history.location.replace(`/${this.catalogue}/documents`)
        dialog.alert({ title: 'Deleted!', icon: 'success', text: 'The record has been deleted.' })
      }
    })
  },

  save () {
    this.setSaveButtonSaving()
    let request
    try {
      request = this.model.save(null, {
        wait: true,
        success: () => {
          this.resetSaveButton()
          dialog.alert({ title: 'Saved!', icon: 'success' })
          this.saveRequired = false
        },
        error: () => {
          this.resetSaveButton()
        }
      })
    } catch (error) {
      this.resetSaveButton()
      throw error
    }

    // save() returns false when client-side validation fails, so no request is
    // sent and neither callback will fire — re-enable the button immediately.
    if (!request) {
      this.resetSaveButton()
    }
  },

  setSaveButtonSaving () {
    this.$('#editorSave')
      .prop('disabled', true)
      .html('Saving… <i class="fa-solid fa-spinner fa-spin"></i>')
  },

  resetSaveButton () {
    this.$('#editorSave')
      .prop('disabled', false)
      .html('Save <i class="fa-regular fa-save"></i>')
  },

  confirmExit () {
    return dialog.confirm({
      title: 'There are unsaved changes.',
      confirmText: 'Exit without saving',
      danger: true
    }).then((result) => {
      if (result.isConfirmed) {
        this.exit()
      }
    })
  },

  attemptExit () {
    if (this.saveRequired === true) {
      this.confirmExit()
    } else {
      this.exit()
    }
  },

  exit () {
    _.invoke(this.sections, 'remove')
    $('body').toggleClass('edit-mode')
    this.remove()

    if ((Backbone.history.location.pathname === `/${this.catalogue}/documents`) && !this.model.isNew()) {
      Backbone.history.location.replace(`/documents/${this.model.get('id')}`)
    } else {
      Backbone.history.location.reload()
    }
  },

  back () {
    this.navigate(this.currentStep - 1)
  },

  next () {
    this.navigate(this.currentStep + 1)
  },

  direct (event) {
    let node = event.currentTarget
    let step = 0
    while (node !== null) {
      step++
      node = node.previousElementSibling
    }

    this.navigate(step)
  },

  navigate (newStep) {
    const $nav = this.$('.editor-nav span')
    const maxStep = $nav.length
    this.currentStep = newStep
    if (this.currentStep < 1) { this.currentStep = 1 }
    if (this.currentStep > maxStep) { this.currentStep = maxStep }

    if (this.currentStep === 1) {
      this.$('#editorBack').prop('disabled', true)
    } else {
      this.$('#editorBack').prop('disabled', false)
    }

    if (this.currentStep === maxStep) {
      this.$('#editorNext').prop('disabled', true)
    } else {
      this.$('#editorNext').prop('disabled', false)
    }

    $nav.filter('.active').toggleClass('active')
    this.$($nav[this.currentStep - 1]).toggleClass('active')

    _.each(this.sections, (section, index) => {
      let method = 'hide'
      if ((this.currentStep - 1) === index) {
        method = 'show'
      }
      _.invoke(section.views, method)
    })
  },

  render () {
    this.$el.html(this.template(this.model.attributes))
    $('body').toggleClass('edit-mode')
    $('#search').toggleClass('search')
    // Section views render themselves in initialize. Rendering them again here would replace
    // what they built there, such as the rows of a record's saved contacts (dri-one #451).
    this.sections.forEach(section => {
      section.views.forEach(view => {
        this.$('#editor').append(view.el)
      })
    })
    return this
  }
})
