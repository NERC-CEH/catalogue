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
    if (this.model.get('check')) this.getServerState(7000)
  },

  getServerState (timeout = 0, callback) {
    const that = this
    setTimeout($.ajax({
      url: `${this.url}/${this.model.get('datastore')}?path=${encodeURIComponent(this.model.get('path'))}`,
      dataType: 'json',
      success (response) {
        that.model.update(response.data)
        if (callback) callback()
      }
    }), timeout)
  },

  // TODO: turn modal into a view as used in multiple places
  showModal (title, body, action) {
    const $documentUploadModal = $('#documentUploadModal')
    $('.modal-title', $documentUploadModal).html(title)
    $('.modal-body', $documentUploadModal).html(body)
    $('.modal-accept', $documentUploadModal).off('click')
    $('.modal-accept', $documentUploadModal).on('click', action)
    $documentUploadModal.modal('show')
  },

  accept (event) {
    const currentClasses = this.showInProgress(event)
    this.request(
      event,
      `${this.url}/${this.model.get('datastore')}/accept?path=${encodeURIComponent(this.model.get('path'))}`,
      'POST',
      () => this.getServerState(3000, () => this.showNormal(event, currentClasses))
    )
  },

  showCancel (event) {
    this.showConfirm(
      event,
      `Cancel moving file: ${this.model.get('path')}?`,
      this.cancel
    )
  },

  cancel (event) {
    const currentClasses = this.showInProgress(event)
    this.request(
      event,
      `${this.url}/${this.model.get('datastore')}/cancel?path=${encodeURIComponent(this.model.get('path'))}`,
      'POST',
      () => this.getServerState(3000, () => this.showNormal(event, currentClasses))
    )
  },

  request (event, url, method, success) {
    const that = this
    $.ajax({
      url,
      type: method,
      success,
      error () {
        that.showInError(event)
      }
    })
  },

  showConfirm (event, message, func) {
    const currentClasses = this.showInProgress(event)
    if (window.confirm(message)) {
      func(event)
    } else {
      this.showNormal(event, currentClasses)
    }
  },

  showDelete (event) {
    this.showConfirm(
      event,
      `Delete file: ${this.model.get('path')}?`,
      this.delete
    )
  },

  showIgnore (event) {
    this.showInProgress(event)
    this.showModal(
      `Ignore the error for ${this.model.get('path')}?`,
      `You are about to ignore the error for<br/><b>${this.model.get('path')}</b><br/>You will lose all infomation about this file if you continue with this action.<br/>This will permanently delete the file.`,
      function () {
        this.delete(event)
      })
  },

  delete (event) {
    const that = this
    $.ajax({
      url: `${this.url}/${this.model.get('datastore')}?path=${encodeURIComponent(this.model.get('path'))}`,
      type: 'DELETE',
      success () {
        that.remove()
        that.collection.remove(this.model)
      },
      error () {
        that.showInError(event)
      }
    })
  },

  expand (event) {
    this.$(event.currentTarget)
      .parent()
      .toggleClass('is-collapsed')
  },

  move (event, to) {
    const that = this
    this.showInProgress(event)
    $.ajax({
      url: `${this.url}/${this.model.get('datastore')}/move?path=${encodeURIComponent(this.model.get('path'))}&to=${to}`,
      type: 'POST',
      success () {
        that.remove()
        that.collection.remove(that.model)
        that.datastore.add(that.model.copy(to))
      },
      error () {
        that.showInError(event)
      }
    })
  },

  moveDatastore (event) {
    this.move(event, 'eidchub')
  },

  moveMetadata (event) {
    this.move(event, 'supporting-documents')
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
    const that = this
    const currentClasses = this.showInProgress(event)
    $.ajax({
      url: `${this.url}/${this.model.get('datastore')}/validate?path=${encodeURIComponent(this.model.get('path'))}`,
      type: 'POST',
      success () {
        that.getServerState(5000, () => { that.showNormal(event, currentClasses) })
      },
      error () {
        that.showInError(event)
      }
    })
  }
})
