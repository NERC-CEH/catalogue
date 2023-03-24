
import _ from 'underscore'
import Backbone from 'backbone'
import template from './FileRow.tpl'

export default Backbone.View.extend({

  events: {
    'click .panel-heading': 'expand',
    'click .accept': 'accept',
    'click .cancel': 'cancel',
    'click .delete': 'delete',
    'click .ignore': 'ignore',
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
    if (this.model.get('check')) this.getServerState(this, 3000)
  },

  getServerState: (self, timeout = 0, callback) => {
    setTimeout(
      $.ajax,
      timeout,
      {
        url: `${self.url}/${self.model.get('datastore')}?path=${encodeURIComponent(self.model.get('path'))}`,
        method: 'GET',
        success: (response) => {
          self.model.update(response.data[0])
          if (callback) callback()
        },
        error: self.showInError
      }
    )
  },

  accept (event) {
    const currentClasses = this.showInProgress(event)
    this.request(
      this,
      event,
      `${this.url}/${this.model.get('datastore')}/accept?path=${encodeURIComponent(this.model.get('path'))}`,
      'POST',
      () => this.getServerState(this, 3000, () => this.showNormal(event, currentClasses))
    )
  },

  cancel (event) {
    const currentClasses = this.showInProgress(event)
    if (window.confirm(`Cancel moving file: ${this.model.get('path')}?`)) {
      this.request(
        this,
        event,
        `${this.url}/${this.model.get('datastore')}/cancel?path=${encodeURIComponent(this.model.get('path'))}`,
        'POST',
        () => this.getServerState(this, 3000, () => this.showNormal(event, currentClasses))
      )
    } else {
      this.showNormal(event, currentClasses)
    }
  },

  request (self, event, url, method, success) {
    $.ajax({
      url,
      type: method,
      success,
      error () {
        self.showInError(event)
      }
    })
  },

  delete (event) {
    const currentClasses = this.showInProgress(event)
    if (window.confirm(`Delete file: ${this.model.get('path')}?`)) {
      this.request(
        this,
        event,
        `${this.url}/${this.model.get('datastore')}?path=${encodeURIComponent(this.model.get('path'))}`,
        'DELETE',
        () => {
          this.remove()
          this.collection.remove(this.model)
        }
      )
    } else {
      this.showNormal(event, currentClasses)
    }
  },

  ignore (event) {
    const currentClasses = this.showInProgress(event)
    if (window.confirm(`Ignore file: ${this.model.get('path')}? This file will be unregistered from the system`)) {
      this.request(
        this,
        event,
        `${this.url}/${this.model.get('datastore')}/unregister?path=${encodeURIComponent(this.model.get('path'))}`,
        'POST',
        () => {
          this.remove()
          this.collection.remove(this.model)
        }
      )
    } else {
      this.showNormal(event, currentClasses)
    }
  },

  expand (event) {
    this.$(event.currentTarget)
      .parent()
      .toggleClass('is-collapsed')
  },

  move (event, to, toCollection) {
    this.request(
      this,
      event,
      `${this.url}/${this.model.get('datastore')}/move?path=${encodeURIComponent(this.model.get('path'))}&to=${to}`,
      'POST',
      () => {
        this.remove()
        this.collection.remove(this.model)
        const toModel = this.model.copy(to)
        toCollection.add(toModel)
      }
    )
  },

  moveDatastore (event) {
    this.move(event, 'eidchub', this.datastore)
  },

  moveMetadata (event) {
    this.move(event, 'supporting-documents', this.metadata)
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
    $icon.attr('class', 'btn-icon fa-solid fa-sync fa-spin')
    return current
  },

  showNormal (event, classes) {
    const $el = this.$(event.currentTarget)
    $el.attr('disabled', false)
    $el.find('i').attr('class', classes)
  },

  showInError (event) {
    const $el = $(event.currentTarget)
    $el.attr('disabled', true)
    $el.find('i').attr('class', 'btn-icon fa fa-exclamation-triangle')
  },

  validate (event) {
    const currentClasses = this.showInProgress(event)
    this.request(
      this,
      event,
      `${this.url}/${this.model.get('datastore')}/validate?path=${encodeURIComponent(this.model.get('path'))}`,
      'POST',
      () => this.getServerState(this, 3000, () => this.showNormal(event, currentClasses))
    )
  }
})
