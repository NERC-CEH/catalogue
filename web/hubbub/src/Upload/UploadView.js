import $ from 'jquery'
import Backbone from 'backbone'
import { File, FileCollection, FileView } from '../File'
import DropzoneView from './DropzoneView'

export default Backbone.View.extend({

  events: {
    'click .finish': 'finish',
    'click .hash-dropbox': 'hashDropbox',
    'click .load.datastore': 'loadDatastore',
    'click .load.data': 'loadDropbox',
    'click .load.metadata': 'loadMetadata',
    'click .move-all': 'moveAllDatastore',
    'click .reschedule': 'reschedule',
    'click .schedule': 'schedule',
    'click .validate-all': 'validateAll'
  },

  initialize () {
    const that = this
    if (this.$('.dropzone-container').length) {
      function dropzoneSuccess (file) {
        const filename = file.name.toLowerCase().replaceAll(' ', '-')
        const model = new File({
          bytes: file.size,
          path: filename,
          status: 'WRITING',
          check: false
        })
        that.dropbox.add(model)
        $(file.previewElement).remove()
      }
      /* eslint-disable no-new */
      new DropzoneView({
        el: '.dropzone-container',
        success: dropzoneSuccess,
        url: this.model.url()
      })
      /* eslint-enable no-new */
    }

    this.$datastore = this.$('.datastore-files')
    this.datastore = new FileCollection()
    this.listenTo(this.datastore, 'reset', function (collection) { this.addAll(collection, this.$datastore) })
    this.listenTo(this.datastore, 'add', function (model) { this.addOne(this.datastore, this.$datastore, model) })
    this.listenTo(this.datastore, 'update', function () { this.showEmptyStorage(this.datastore, this.$datastore, 'datastore') })

    this.$dropbox = this.$('.data-files')
    this.dropbox = new FileCollection()
    this.listenTo(this.dropbox, 'reset', function (collection) { this.addAll(collection, this.$dropbox) })
    this.listenTo(this.dropbox, 'add', function (model) { this.addOne(this.dropbox, this.$dropbox, model) })
    this.listenTo(this.dropbox, 'update', function () { this.showEmptyStorage(this.dropbox, this.$dropbox, 'data') })

    this.$metadata = this.$('.metadata-files')
    this.metadata = new FileCollection()
    this.listenTo(this.metadata, 'reset', function (collection) { this.addAll(collection, this.$metadata) })
    this.listenTo(this.metadata, 'add', function (model) { this.addOne(this.metadata, this.$metadata, model) })
    this.listenTo(this.metadata, 'update', function () { this.showEmptyStorage(this.metadata, this.$metadata, 'metadata') })

    const $datastoreData = $('#datastore-data')
    if ($datastoreData.length) {
      const response = JSON.parse($datastoreData.text())
      this.model.set({
        datastorePage: response.meta.currentPage,
        datastoreSize: response.meta.pageSize,
        datastoreLastPage: response.meta.lastPage
      })
      this.datastore.reset(response.data)
    }

    const $dropboxData = $('#dropbox-data')
    if ($dropboxData.length) {
      const response = JSON.parse($dropboxData.text())
      this.model.set({
        dropboxPage: response.meta.currentPage,
        dropboxSize: response.meta.pageSize,
        dropboxLastPage: response.meta.lastPage
      })
      this.dropbox.reset(response.data)
    }

    const $metadataData = $('#metadata-data')
    if ($metadataData.length) {
      const response = JSON.parse($metadataData.text())
      this.model.set({
        metadataPage: response.meta.currentPage,
        metadataSize: response.meta.pageSize,
        metadataLastPage: response.meta.lastPage
      })
      this.metadata.reset(response.data)
    }
  },

  addOne (collection, $container, model) {
    const view = new FileView({
      collection,
      datastore: this.datastore,
      metadata: this.metadata,
      model,
      url: this.model.url()
    })
    $container.append(view.render().el)
  },

  addAll (collection, $container) {
    $container.empty()
    collection.each(model => this.addOne(collection, $container, model))
  },

  loadMore (event, name, path, collection) {
    const that = this
    const nextPage = this.model.get(`${name}Page`) + 1
    const size = this.model.get(`${name}Size`)
    const lastPage = this.model.get(`${name}LastPage`)
    if (nextPage <= lastPage) {
      const currentClasses = this.showInProgress(event)
      return $.ajax({
        url: `${this.model.url()}/${path}?page=${nextPage}&size=${size}`,
        success (response) {
          that.showNormal(event, currentClasses)
          collection.add(response.data)
          that.model.set(`${name}Page`, response.meta.currentPage)
        },
        error () {
          that.showInError(event)
        }
      })
    }
  },

  loadDatastore (event) {
    this.loadMore(event, 'datastore', 'eidchub', this.datastore)
  },

  loadDropbox (event) {
    this.loadMore(event, 'dropbox', 'dropbox', this.dropbox)
  },

  loadMetadata (event) {
    this.loadMore(event, 'metadata', 'supporting-documents', this.metadata)
  },

  showEmptyStorage (collection, $container, title) {
    if (collection.length === 0) {
      $container.append(`<h3 class="no-documents text-center">NO FILES IN ${title.toUpperCase()}</h3>`)
    } else {
      $container.find('.no-documents').remove()
    }
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
    $icon.attr('class', classes)
  },

  showInError (event) {
    const $el = $(event.currentTarget)
    $el.attr('disabled', true)
    const $icon = $('i', $el)
    $icon.attr('class', 'btn-icon fa fa-exclamation-triangle')
  },

  finish (event) {
    const that = this
    const currentClasses = this.showInProgress(event)
    return $.ajax({
      url: `${this.model.url()}/finish`,
      type: 'POST',
      success () {
        that.showNormal(event, currentClasses)
        that.redirectToDocuments()
      },
      error () {
        that.showInError(event)
      }
    })
  },

  redirectToDocuments () {
    window.location.assign(`/documents/${this.model.get('id')}`)
  },

  hashDropbox (event) {
    const that = this
    const currentClasses = this.showInProgress(event)
    return $.ajax({
      url: `${this.model.url()}/hash`,
      type: 'POST',
      success () {
        that.collectionSuccess(event, currentClasses, 'dropbox', 'dropbox', that.dropbox)
      },
      error () {
        that.showInError(event)
      }
    })
  },

  moveAllDatastore (event) {
    const that = this
    const currentClasses = this.showInProgress(event)
    $.ajax({
      url: `${this.model.url()}/dropbox/move?to=eidchub`,
      type: 'POST',
      success () {
        that.dropbox.each(model => that.addOne(that.datastore, that.$datastore, model.copy()))
        that.dropbox.reset()
        that.showNormal(event, currentClasses)
      },
      error () {
        that.showInError(event)
      }
    })
  },

  reschedule (event) {
    const that = this
    const currentClasses = this.showInProgress(event)
    $.ajax({
      url: `${this.model.url()}/reschedule`,
      type: 'POST',
      success () {
        that.showNormal(event, currentClasses)
        that.reloadPage()
      },
      error () {
        that.showInError(event)
      }
    })
  },

  reloadPage () {
    window.location.reload()
  },

  schedule (event) {
    const that = this
    const currentClasses = this.showInProgress(event)
    return $.ajax({
      url: `${this.model.url()}/schedule`,
      type: 'POST',
      success () {
        that.showNormal(event, currentClasses)
        that.reloadPage()
      },
      error () {
        that.showInError(event)
      }
    })
  },

  validateAll (event) {
    this.validate(event, 'dropbox', 'dropbox', this.dropbox)
    this.validate(event, 'metadata', 'supporting-documents', this.metadata)
    this.validate(event, 'datastore', 'eidchub', this.datastore)
  },

  validate (event, name, datastore, collection) {
    const that = this
    const currentClasses = this.showInProgress(event)
    $.ajax({
      url: `${this.model.url()}/${datastore}/validate`,
      type: 'POST',
      success () {
        that.collectionSuccess(event, currentClasses, name, datastore, collection)
      },
      error () {
        that.showInError(event)
      }
    })
  },

  collectionSuccess (event, currentClasses, name, datastore, collection) {
    function showNormal () { this.showNormal(event, currentClasses) }
    function collectionState () { this.getServerState(name, datastore, collection, showNormal) }
    setTimeout(collectionState, 7000)
  },

  getServerState (name, datastore, collection, callback) {
    const page = 1
    const collectionSize = collection.length
    let size = (this.model.has(`${name}Size`)) ? this.model.get(`${name}Size`) : 20
    if (collectionSize > size) { size = collectionSize }
    this.model.set(`${name}Page`, page)
    this.model.set(`${name}Size`, size)

    $.ajax({
      url: `${this.model.url()}/${datastore}?page=${page}&size=${size}`,
      dataType: 'json',
      success (response) {
        collection.reset(response.data)
        if (callback) { callback() }
      }
    })
  }
})
