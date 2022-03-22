import $ from 'jquery'
import _ from 'underscore'
import Backbone from 'backbone'
import template from './FileRow.tpl'

export default Backbone.View.extend({

  events: {
    'click .panel-heading': 'expand',
    'click .accept': 'accept',
    'click .cancel': 'showCancel',
    'click .delete': 'showDelete',
    'click .ignore': 'showIgnore',
    'click .move-datastore': 'moveDatastore',
    'click .move-metadata': 'moveMetadata',
    'click .validate': 'validate'
  },

  initialize (options) {
    this.template = _.template(template)
    this.url = options.url
    this.datastore = options.datastore
    this.metadata = options.metadata
    this.listenTo(this.model, 'change', this.render)
    if (this.model.get('check')) {
      setTimeout(
        () => this.getServerState(),
        7000
      )
    }
    this.listenTo(this.model, 'change', this.render)
  },

  getServerState (callback) {
    return $.ajax({
      url: `${this.url}/${this.model.get('datastore')}?path=${encodeURIComponent(this.model.get('path'))}`,
      dataType: 'json',
      success (response) {
        this.model.update(response.data)
        if (callback) { callback() }
      },
      error (err) {
        console.error('error', err)
      }
    })
  },

  // TODO: turn modal into a view as used in multiple places
  showModal (title, body, action) {
    const $documentUploadModal = $('#documentUploadModal')
    $('.modal-title', $documentUploadModal).html(title)
    $('.modal-body', $documentUploadModal).html(body)
    $('.modal-accept', $documentUploadModal).unbind('click')
    $('.modal-accept', $documentUploadModal).click(action.bind(this))
    $documentUploadModal.modal('show')
  },

  accept (event) {
    const currentClasses = this.showInProgress(event)
    $.ajax({
      url: `${this.url}/accept?path=${encodeURIComponent(this.model.get('path'))}`,
      type: 'POST',
      success: () => {
        return setTimeout(
          () => {
            return this.getServerState(function () { return this.showNormal(event, currentClasses) })
          }
          ,
          3000
        )
      },
      error: err => {
        this.showInError(event)
        console.error('error', err)
      }
    })
  },

  showCancel (event) {
    this.showInProgress(event)
    this.showModal(
      `Cancel moving ${this.model.get('name')}?`,
      'This will not stop the file from being moved.<br/>Only do this if you feel the file is no longer moving to the desired destination,<br/>e.g. due to a server error.',
      function () {
        this.cancel(event)
      })
  },

  cancel (event) {
    const currentClasses = this.showInProgress(event)
    $.ajax({
      url: `${this.url}/cancel?path=${encodeURIComponent(this.model.get('path'))}`,
      type: 'POST',
      success: () => {
        setTimeout(
          () => {
            this.getServerState(function () { return this.showNormal(event, currentClasses) })
          }
          ,
          3000
        )
      },
      error: err => {
        this.showInError(event)
        console.error('error', err)
      }
    })
  },

  showDelete (event) {
    this.showInProgress(event)
    this.showModal(
      `Delete ${this.model.get('name')}?`,
      `This will permanently delete the file<br/><b>${this.model.get('path')}</b>`,
      function () {
        this.delete(event)
      })
  },

  showIgnore (event) {
    this.showInProgress(event)
    this.showModal(
      `Ignore the error for ${this.model.get('name')}?`,
      `You are about to ignore the error for<br/><b>${this.model.get('path')}</b><br/>You will lose all infomation about this file if you continue with this action.<br/>This will permanently delete the file.`,
      function () {
        this.delete(event)
      })
  },

  delete (event) {
    this.showInProgress(event)
    $.ajax({
      url: `${this.url}?path=${encodeURIComponent(this.model.get('path'))}`,
      type: 'DELETE',
      success: () => {
        this.remove()
        this.collection.remove(this.model)
      },
      error: err => {
        this.showInError(event)
        console.error('error', err)
      }
    })
  },

  expand (event) {
    this.$(event.currentTarget)
      .parent()
      .toggleClass('is-collapsed')
  },

  moveDatastore (event) {
    this.showInProgress(event)
    $.ajax({
      url: `${this.url}/move-datastore?path=${encodeURIComponent(this.model.get('path'))}`,
      type: 'POST',
      success: () => {
        delete
        this.remove()
        this.collection.remove(this.model)
        this.datastore.add(this.model.copy('eidchub'))
      },
      error: err => {
        this.showInError(event)
        console.error('error', err)
      }
    })
  },

  moveMetadata (event) {
    this.showInProgress(event)
    $.ajax({
      url: `${this.url}/move-metadata?path=${encodeURIComponent(this.model.get('path'))}`,
      type: 'POST',
      success: () => {
        this.remove()
        this.collection.remove(this.model)
        return this.metadata.add(this.model.copy('supporting-documents'))
      },
      error: err => {
        this.showInError(event)
        console.error('error', err)
      }
    })
  },

  render () {
    this.$el.html(this.template(this.model.attributes))
    return this
  },

  showInProgress (event) {
    const $el = this.$(event.currentTarget)
    $el.attr('disabled', true)
    const $icon = $el.find('i')
    const current = $icon.attr('class')
    $icon.attr('class', 'btn-icon fas fa-sync fa-spin')
    return current
  },

  showNormal (event, classes) {
    const $el = this.$(event.currentTarget)
    $el.attr('disabled', false)
    $el.find('i').attr('class', classes)
  },

  showInError (event) {
    const $el = this.$(event.currentTarget)
    $el.attr('disabled', true)
    $el.find('i').attr('class', 'btn-icon fa fa-exclamation-triangle')
  },

  validate (event) {
    const currentClasses = this.showInProgress(event)
    $.ajax({
      url: `${this.url}/${this.model.get('datastore')}/validate?path=${encodeURIComponent(this.model.get('path'))}`,
      type: 'POST',
      success: () => {
        setTimeout(
          () => {
            this.getServerState(function () { return this.showNormal(event, currentClasses) })
          }
          ,
          5000
        )
      },
      error: err => {
        this.showInError(event)
        console.error('error', err)
      }
    })
  }
})
