import $ from 'jquery'
import Backbone from 'backbone'
import { File, FileCollection } from '../File'
import { TableView } from '../table'
import DropzoneView from './DropzoneView'
import { ExpectedFilesView } from '../Expected'
import { dialog } from '../../../dialog/src/dialog'

export default Backbone.View.extend({

  events: {
    'click .finish': 'finish',
    'click .hash-dropbox': 'hashDropbox',
    'click .register-dropbox': 'registerDropbox',
    'click .load.datastore': 'loadDatastore',
    'click .load.data': 'loadDropbox',
    'click .load.metadata': 'loadMetadata',
    'click .move-all': 'moveAllDatastore',
    'click .reschedule': 'reschedule',
    'click .schedule': 'schedule',
    'click .validate-all': 'validateAll'
  },

  initialize () {
    this.dropzoneData = []
    this.zipUploaded = false
    if (this.$('.dropzone-container').length) {
      this.dropzoneView = new DropzoneView({
        el: '.dropzone-container',
        success: file => this.uploaded(file),
        complete: () => this.queueCompleted(),
        url: this.model.url()
      })
    }

    this.dropboxView = null
    this.datastoreView = null
    this.metadataView = null

    this.$datastore = this.$('.datastore-files')
    this.datastore = new FileCollection()
    this.listenTo(this.datastore, 'reset', (collection) => this.addAll('datastoreView', collection, this.$datastore))
    this.listenTo(this.datastore, 'update', () => this.showEmptyStorage(this.datastore, this.$datastore, 'datastore'))
    this.listenTo(this.model, 'change:datastoreTotalFiles', () => this.renderTotalFiles('datastore'))

    this.$dropbox = this.$('.data-files')
    this.dropbox = new FileCollection()
    this.listenTo(this.dropbox, 'reset', (collection) => this.addAll('dropboxView', collection, this.$dropbox))
    this.listenTo(this.dropbox, 'update', () => this.showEmptyStorage(this.dropbox, this.$dropbox, 'data'))
    this.listenTo(this.model, 'change:dropboxTotalFiles', () => this.renderTotalFiles('dropbox'))

    this.$metadata = this.$('.metadata-files')
    this.metadata = new FileCollection()
    this.listenTo(this.metadata, 'reset', (collection) => this.addAll('metadataView', collection, this.$metadata))
    this.listenTo(this.metadata, 'update', () => this.showEmptyStorage(this.metadata, this.$metadata, 'metadata'))
    this.listenTo(this.model, 'change:metadataTotalFiles', () => this.renderTotalFiles('metadata'))

    // Which listings came from Hubbub: a dropbox filled only by this page's own uploads isn't one
    this.listed = { dropbox: $('#dropbox-data').length > 0, datastore: $('#datastore-data').length > 0 }

    const $datastoreData = $('#datastore-data')
    if ($datastoreData.length) {
      const response = JSON.parse($datastoreData.text())
      this.model.set({
        datastorePage: response.meta.currentPage,
        datastoreSize: response.meta.pageSize,
        datastoreLastPage: response.meta.lastPage,
        datastoreTotalFiles: response.meta.totalFiles
      })
      this.setLoadMoreButton('datastore')
      this.datastore.reset(response.data)
    }

    const $dropboxData = $('#dropbox-data')
    if ($dropboxData.length) {
      const response = JSON.parse($dropboxData.text())
      this.model.set({
        dropboxPage: response.meta.currentPage,
        dropboxSize: response.meta.pageSize,
        dropboxLastPage: response.meta.lastPage,
        dropboxTotalFiles: response.meta.totalFiles
      })
      this.setLoadMoreButton('dropbox')
      this.dropbox.reset(response.data)
    }

    const $metadataData = $('#metadata-data')
    if ($metadataData.length) {
      const response = JSON.parse($metadataData.text())
      this.model.set({
        metadataPage: response.meta.currentPage,
        metadataSize: response.meta.pageSize,
        metadataLastPage: response.meta.lastPage,
        metadataTotalFiles: response.meta.totalFiles
      })
      this.setLoadMoreButton('metadata')
      this.metadata.reset(response.data)
    }

    this.listenTo(this.dropbox, 'addBatch', (models) => this.addBatch(this.dropboxView, this.dropbox, models))
    this.listenTo(this.datastore, 'addBatch', (models) => this.addBatch(this.datastoreView, this.datastore, models))
    this.listenTo(this.metadata, 'addBatch', (models) => this.addBatch(this.metadataView, this.metadata, models))

    this.initializeExpectedFiles()
  },

  // The agreed file listing, ticked off as the dropbox and datastore listings change (dri-one #536)
  initializeExpectedFiles () {
    const $expectedData = $('#expected-data')
    if (!$expectedData.length) return

    const $datastorePaths = $('#datastore-paths-data')
    // A depositor's page has only the datastore's paths: each one is there, its gone files left out
    this.datastorePaths = $datastorePaths.length ? JSON.parse($datastorePaths.text()) : null

    this.expectedFilesView = new ExpectedFilesView({
      el: this.$('.expected-files')[0],
      expected: JSON.parse($expectedData.text()),
      sources: () => this.expectedSources()
    })
    const render = () => this.expectedFilesView.render()
    // update, not add and remove: it comes once per batch, where they come once per file
    ;[this.dropbox, this.datastore].forEach(collection => this.listenTo(collection, 'reset update change:status', render))
    this.listenTo(this.model, 'change:dropboxPage change:dropboxLastPage change:datastorePage change:datastoreLastPage', render)
    render()
  },

  expectedSources () {
    const listed = file => ({ path: file.get('path'), status: file.get('status'), bytes: file.get('bytes') })
    const uploads = [...this.dropbox.map(listed), ...this.datastore.map(listed)]
    const datastorePaths = !this.listed.datastore && this.datastorePaths
    // A depositor's page has the datastore's paths but not their sizes, so those aren't size-checked
    if (datastorePaths) {
      uploads.push(...(datastorePaths.paths || []).map(path => ({ path, status: 'VALID' })))
    }
    const datastoreComplete = this.listed.datastore
      ? this.checkLastPage('datastore')
      : Boolean(datastorePaths && datastorePaths.complete)
    return {
      uploads,
      complete: this.listed.dropbox && this.checkLastPage('dropbox') && datastoreComplete,
      hasListings: this.listed.dropbox || this.listed.datastore || Boolean(datastorePaths)
    }
  },

  uploaded (file) {
    const filename = (file?.fullPath ?? file.name).toLowerCase().replaceAll(' ', '-')
    // A zip is unpacked on the server, so the page is reloaded to show its files, but only once the
    // queue is done: reloading now would cancel the uploads still in flight (dri-one #459)
    if (filename.endsWith('.zip')) {
      this.zipUploaded = true
    }
    const model = new File({
      bytes: file.size,
      datasetId: this.model.get('id'),
      datastore: 'dropbox',
      path: filename,
      status: 'NO_HASH',
      check: false
    })
    this.dropzoneData.push(model)
    $(file.previewElement).remove()
  },

  queueCompleted () {
    if (this.zipUploaded) {
      this.zipUploaded = false
      this.reloadPage()
      return
    }
    this.dropbox.length > 0 ? this.dropbox.trigger('addBatch', this.dropzoneData) : this.dropbox.reset(this.dropzoneData)
    this.model.set('dropboxTotalFiles', (this.model.get('dropboxTotalFiles') ?? 0) + this.dropzoneData.length)
    this.dropzoneData = []
  },

  addBatch (tableView, collection, models) {
    if (models.length === 0) return

    collection.add(models)
    const rows = models.map(model => tableView.prepareSingleTableData(model))
    tableView.dataTable.rows.add(rows).draw(false)
    tableView.updateSelectList()
  },

  addAll (tableView, collection, $container) {
    // A refresh can come back empty (everything moved or deleted): clear the old rows rather than
    // leave them on screen. reset fires no update, so the empty message is shown here too.
    if (collection.length === 0) {
      if (this[tableView]) this[tableView].dataTable.clear().draw()
      const title = { datastoreView: 'datastore', dropboxView: 'data', metadataView: 'metadata' }[tableView]
      this.showEmptyStorage(collection, $container, title)
      return
    }
    this.setLoadMoreButton(tableView.replace('View', ''))
    $container.empty()
    const view = new TableView({
      collection,
      datastore: this.datastore,
      metadata: this.metadata,
      model: this.model,
      el: $container[0]
    })

    view.render()
    this[tableView] = view
  },

  setLoadMoreButton (name) {
    if (this.checkLastPage(name)) {
      (name === 'dropbox') ? this.$('.load.data').hide() : this.$('.load.' + name).hide()
    } else {
      (name === 'dropbox') ? this.$('.load.data').show() : this.$('.load.' + name).show()
    }
  },

  checkLastPage (name) {
    const currentPage = this.model.get(`${name}Page`)
    const lastPage = this.model.get(`${name}LastPage`)
    if (!currentPage || !lastPage) return true

    const nextPage = currentPage + 1
    return nextPage > lastPage
  },

  loadMore (event, name, path, collection) {
    const nextPage = this.model.get(`${name}Page`) + 1
    const size = this.model.get(`${name}Size`)
    if (!this.checkLastPage(name)) {
      const currentClasses = this.showInProgress(event)
      return $.ajax({
        url: `${this.model.url()}/${path}?page=${nextPage}&size=${size}`,
        success: (response) => {
          const models = response.data.map(attrs => new File(attrs))
          collection.trigger('addBatch', models)
          this.model.set(`${name}Page`, response.meta.currentPage)
          this.showNormal(event, currentClasses)
          this.setLoadMoreButton(name)
        },
        error: () => this.showInError(event)
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

  renderTotalFiles (datastore) {
    const $totalFiles = this.$(`.document-upload-section.${datastore} .total-files`)
    const totalFiles = this.model.get(`${datastore}TotalFiles`)
    const fileText = totalFiles === 1 ? 'file' : 'files'
    $totalFiles.text(`${Number(totalFiles).toLocaleString()} ${fileText}`)
  },

  showEmptyStorage (collection, $container, title) {
    const store = (title === 'data') ? 'dropbox' : title
    if (collection.length === 0 && this.checkLastPage(store)) {
      // Once: the page renders this message for an empty store, which then loads empty too
      if (!$container.find('.no-documents').length) {
        $container.append(`<h3 class="no-documents text-center">NO FILES IN ${title.toUpperCase()}</h3>`)
      }
    } else {
      $container.find('.no-documents').remove()
    }
  },

  showInProgress (event) {
    const $el = $(event.currentTarget)
    $el.attr('disabled', true)
    const $icon = $('i', $el)
    const current = $icon.attr('class')
    $icon.attr('class', 'btn-icon fa-fw fa-solid fa-sync fa-spin')
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
    $icon.attr('class', 'btn-icon fa-fw fa fa-exclamation-triangle')
  },

  pendingUploads () {
    const dropzone = this.dropzoneView?.dropzone
    return dropzone ? dropzone.getUploadingFiles().length + dropzone.getQueuedFiles().length : 0
  },

  // Finishing hands the dataset to the EIDC and removes the depositor's upload permission, and
  // leaving the page cancels any upload still in flight, so it waits for those and asks first
  finish (event) {
    const pending = this.pendingUploads()
    if (pending > 0) {
      return dialog.alert({
        title: 'Still uploading',
        icon: 'info',
        text: `${pending} ${pending === 1 ? 'file is' : 'files are'} still uploading. Wait for them to finish before you finish uploading.`
      })
    }
    // The button, read now: an event's currentTarget is only certain while it is being dispatched
    const button = { currentTarget: event.currentTarget }
    return dialog.confirm({
      title: 'Finish uploading?',
      text: 'You will not be able to add more files to this dataset afterwards.',
      confirmText: 'Finish uploading'
    }).then(({ isConfirmed }) => {
      // Not returned: a failed request is shown on the button by sendFinish, and returned, its
      // rejection would carry on into this promise, which nothing catches
      if (isConfirmed) this.sendFinish(button)
    })
  },

  sendFinish (event) {
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
    const button = { currentTarget: event.currentTarget }
    return dialog.confirm({
      title: 'Move every file to the datastore?',
      text: 'Every file in the dropbox moves, including any not yet validated.',
      confirmText: 'Move every file'
    }).then(({ isConfirmed }) => {
      if (isConfirmed) this.sendMoveAll(button)
    })
  },

  sendMoveAll (event) {
    const that = this
    const currentClasses = this.showInProgress(event)
    $.ajax({
      url: `${this.model.url()}/dropbox/move?to=eidchub`,
      type: 'POST',
      success () {
        that.collectionSuccess(event, currentClasses, 'datastore', 'eidchub', that.datastore, () => {
          // No table when the arrivals were empty as the page loaded
          that.dropboxView?.dataTable.clear().draw()
          that.model.set({
            dropboxPage: 0,
            dropboxLastPage: 0,
            dropboxTotalFiles: 0
          })
          if (that.dropbox.length > 0) {
            that.dropbox.set([])
          } else {
            that.showEmptyStorage(that.dropbox, that.$dropbox, 'data')
          }
        })
      },
      error () {
        that.showInError(event)
      }
    })
  },

  registerDropbox (event) {
    const that = this
    const currentClasses = this.showInProgress(event)
    return $.ajax({
      url: `${this.model.url()}/register`,
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

  // One button for three stores, so it shows the outcome once all three have answered: each used to
  // set it as it finished, and a store finishing last hid another's failure
  validateAll (event) {
    const currentClasses = this.showInProgress(event)
    const stores = [
      ['dropbox', 'dropbox', this.dropbox],
      ['metadata', 'supporting-documents', this.metadata],
      ['datastore', 'eidchub', this.datastore]
    ]
    let waiting = stores.length
    let failed = false
    const done = succeeded => {
      failed = failed || !succeeded
      if (--waiting > 0) return
      failed ? this.showInError(event) : this.showNormal(event, currentClasses)
    }
    stores.forEach(([name, datastore, collection]) => this.validate(event, name, datastore, collection, currentClasses, done))
  },

  // done, if given, is told whether the store validated and refreshed, in place of the button being set
  validate (event, name, datastore, collection, currentClasses, done) {
    $.ajax({
      url: `${this.model.url()}/${datastore}/validate`,
      type: 'POST',
      success: () => this.collectionSuccess(event, currentClasses, name, datastore, collection, undefined, done),
      error: (xhr) => {
        // 400: nothing in that store to validate. Stop the spinner rather than leave it running.
        const succeeded = xhr.status === 400
        if (done) {
          done(succeeded)
        } else if (succeeded) {
          this.showNormal(event, currentClasses)
        } else {
          this.showInError(event)
        }
      }
    })
  },

  collectionSuccess (event, currentClasses, name, datastore, collection, callback, done) {
    setTimeout(
      () => this.getServerState(
        name,
        datastore,
        collection,
        () => {
          if (callback) callback()
          done ? done(true) : this.showNormal(event, currentClasses)
        },
        () => done ? done(false) : this.showInError(event)
      ),
      7000
    )
  },

  getServerState (name, datastore, collection, callback, onError) {
    const page = 1
    const collectionSize = collection.length
    let size = (this.model.has(`${name}Size`)) ? this.model.get(`${name}Size`) : 5000
    if (collectionSize > size) { size = collectionSize }
    this.model.set(`${name}Page`, page)
    this.model.set(`${name}Size`, size)

    const self = this
    $.ajax({
      url: `${this.model.url()}/${datastore}?page=${page}&size=${size}`,
      dataType: 'json',
      success (response) {
        self.listed[name] = true
        collection.reset(response.data)
        self.model.set(`${name}TotalFiles`, response.meta.totalFiles)
        self.model.set(`${name}LastPage`, response.meta.lastPage)
        self.setLoadMoreButton(name)
        if (callback) { callback() }
      },
      error () {
        if (onError) { onError() }
      }
    })
  }
})
