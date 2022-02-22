/* eslint-disable
    no-new,
    no-undef,
    no-useless-escape,
*/
// TODO: This file was created by bulk-decaffeinate.
// Fix any style issues and re-enable lint.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'backbone',
  'cs!collections/upload/hubbub/FileCollection',
  'cs!models/upload/hubbub/File',
  'cs!views/upload/hubbub/FileView',
  'tpl!templates/upload/hubbub/FileRow.tpl',
  'cs!views/upload/hubbub/DropzoneView'
], function ($, Backbone, FileCollection, File, FileView, fileRowTemplate, DropzoneView) {
  return Backbone.View.extend({

    events: {
      'click .finish': 'showFinish',
      'click .load.datastore': 'loadDatastore',
      'click .load.data': 'loadDropbox',
      'click .load.metadata': 'loadMetadata',
      'click .move-all': 'moveAllDatastore',
      'click .reschedule': 'reschedule',
      'click .schedule': 'schedule',
      'click .validate-all': 'validateAll'
    },

    initialize () {
      if (this.$('.dropzone-container').length) {
        const dropzoneSuccess = file => {
          const filename = file.name.toLowerCase().replaceAll(' ', '-')
          const model = new File({
            bytes: file.size,
            name: filename,
            path: `/dropbox/${this.model.get('id')}/${filename}`,
            status: 'WRITING',
            check: true
          })
          this.dropbox.add(model)
          return $(file.previewElement).remove()
        }

        new DropzoneView({
          el: '.dropzone-container',
          success: dropzoneSuccess,
          url: this.model.url()
        })
      }

      this.$datastore = this.$('.datastore-files')
      this.datastore = new FileCollection()
      this.listenTo(this.datastore, 'reset', function (collection) { return this.addAll(collection, this.$datastore) })
      this.listenTo(this.datastore, 'add', function (model) { return this.addOne(this.datastore, this.$datastore, model) })
      this.listenTo(this.datastore, 'update', function () { return this.showEmptyStorage(this.datastore, this.$datastore, 'datastore') })

      this.$dropbox = this.$('.data-files')
      this.dropbox = new FileCollection()
      this.listenTo(this.dropbox, 'reset', function (collection) { return this.addAll(collection, this.$dropbox) })
      this.listenTo(this.dropbox, 'add', function (model) { return this.addOne(this.dropbox, this.$dropbox, model) })
      this.listenTo(this.dropbox, 'update', function () { return this.showEmptyStorage(this.dropbox, this.$dropbox, 'data') })

      this.$metadata = this.$('.metadata-files')
      this.metadata = new FileCollection()
      this.listenTo(this.metadata, 'reset', function (collection) { return this.addAll(collection, this.$metadata) })
      this.listenTo(this.metadata, 'add', function (model) { return this.addOne(this.metadata, this.$metadata, model) })
      this.listenTo(this.metadata, 'update', function () { return this.showEmptyStorage(this.metadata, this.$metadata, 'metadata') })

      const $datastoreData = $('#datastore-data')
      if ($datastoreData.length) { this.datastore.reset(JSON.parse($datastoreData.text())) }

      const $dropboxData = $('#dropbox-data')
      if ($dropboxData.length) { this.dropbox.reset(JSON.parse($dropboxData.text())) }

      const $metadataData = $('#metadata-data')
      if ($metadataData.length) { return this.metadata.reset(JSON.parse($metadataData.text())) }
    },

    addOne (collection, $container, model) {
      const view = new FileView({
        collection,
        datastore: this.datastore,
        metadata: this.metadata,
        model,
        url: this.model.url()
      })
      return $container.append(view.render().el)
    },

    addAll (collection, $container) {
      $container.empty()
      return collection.each(model => this.addOne(collection, $container, model))
    },

    loadMore (event, name, path, collection) {
      const currentClasses = this.showInProgress(event)
      const nextPage = this.model.get(`${name}Page`) + 1
      const size = this.model.get(`${name}Size`)
      return $.ajax({
        url: `${this.model.url()}/${path}?page=${nextPage}&size=${size}`,
        success: data => {
          this.showNormal(event, currentClasses)
          collection.add(data)
          if (data.length === size) {
            return this.model.set(`${name}Page`, nextPage)
          }
        },
        error (err) {
          this.showInError(event)
          return console.error('error', err)
        }
      })
    },

    loadDatastore (event) {
      return this.loadMore(event, 'datastore', 'eidchub', this.datastore)
    },

    loadDropbox (event) {
      return this.loadMore(event, 'dropbox', 'dropbox', this.dropbox)
    },

    loadMetadata (event) {
      return this.loadMore(event, 'metadata', 'supporting-documents', this.metadata)
    },

    showEmptyStorage (collection, $container, title) {
      if (collection.length === 0) {
        return $container.append(`<h3 class=\"no-documents text-center\">NO FILES IN ${title.toUpperCase()}</h3>`)
      } else {
        return $container.find('.no-documents').remove()
      }
    },

    showModal (title, body, action, event) {
      this.showInProgress(event)
      const $modal = $('#documentUploadModal')
      $('.modal-title', $modal).html(title)
      $('.modal-body', $modal).html(body)
      $('.modal-accept', $modal).unbind('click')
      $('.modal-accept', $modal).click(action.bind(this))
      return $modal.modal('show')
    },

    showInProgress (event) {
      const $el = $(event.currentTarget)
      $el.attr('disabled', true)
      const $icon = $('i', $el)
      const current = $icon.attr('class')
      $icon.attr('class', 'btn-icon fas fa-sync fa-spin')
      return current
    },

    showNormal (event, classes) {
      const $el = $(event.currentTarget)
      $el.attr('disabled', false)
      const $icon = $('i', $el)
      return $icon.attr('class', classes)
    },

    showInError (event) {
      const $el = $(event.currentTarget)
      $el.attr('disabled', true)
      const $icon = $('i', $el)
      return $icon.attr('class', 'btn-icon fa fa-exclamation-triangle')
    },

    showFinish (event) {
      return this.showModal(
        'Have you finished uploading files?',
        'You will no longer be able to add, remove or update files.',
        function () { return this.finish(event) },
        event
      )
    },

    finish (event) {
      const currentClasses = this.showInProgress(event)
      return $.ajax({
        url: `${this.model.url()}/finish`,
        type: 'POST',
        success: () => {
          this.showNormal(event, currentClasses)
          return window.location.assign(`/documents/${this.model.get('id')}`)
        },
        error (err) {
          this.showInError(event)
          return console.error('error', err)
        }
      })
    },

    moveAllDatastore (event) {
      const currentClasses = this.showInProgress(event)
      return $.ajax({
        url: `${this.model.url()}/move-all-datastore`,
        type: 'POST',
        success: () => {
          this.dropbox.each(model => this.addOne(this.datastore, this.$datastore, model.copy('/eidchub/')))
          this.dropbox.reset()
          return this.showNormal(event, currentClasses)
        },
        error: err => {
          this.showInError(event)
          return console.error('error', err)
        }
      })
    },

    reschedule (event) {
      const currentClasses = this.showInProgress(event)
      return $.ajax({
        url: `${this.model.url()}/reschedule`,
        type: 'POST',
        success: () => {
          this.showNormal(event, currentClasses)
          //        TODO: re-render based on changed state of model rather than reloading
          return window.location.reload()
        },
        error: err => {
          this.showInError(event)
          return console.error('error', err)
        }
      })
    },

    schedule (event) {
      const currentClasses = this.showInProgress(event)
      return $.ajax({
        url: `${this.model.url()}/schedule`,
        type: 'POST',
        success: () => {
          this.showNormal(event, currentClasses)
          //        TODO: re-render based on changed state of model rather than reloading
          return window.location.reload()
        },
        error: err => {
          this.showInError(event)
          return console.error('error', err)
        }
      })
    },

    validateAll (event) {
      const currentClasses = this.showInProgress(event)
      return $.ajax({
        url: `${this.model.url()}/validate`,
        type: 'POST',
        success: () => {
          const showNormal = () => this.showNormal(event, currentClasses)
          const datastore = () => this.getServerState('datastore', 'eidchub', this.datastore, showNormal)
          const dropbox = () => this.getServerState('dropbox', 'dropbox', this.dropbox, datastore)
          const metadata = () => this.getServerState('metadata', 'supporting-documents', this.metadata, dropbox)
          return setTimeout(metadata, 7000)
        },
        error: err => {
          this.showInError(event)
          return console.error('error', err)
        }
      })
    },

    getServerState (name, path, collection, callback) {
      const page = 1
      const collectionSize = collection.length
      let size = this.model.get(`${name}Size`)
      if (collectionSize > size) { size = collectionSize }
      this.model.set({ [`${name}Page`]: page })
      this.model.set({ [`${name}Size`]: size })

      return $.ajax({
        url: `${this.model.url()}/${path}?page=${page}&size=${size}`,
        dataType: 'json',
        success (data) {
          collection.reset(data)
          if (callback) { return callback() }
        }
      })
    }
  })
})
