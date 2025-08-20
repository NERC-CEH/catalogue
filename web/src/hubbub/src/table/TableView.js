import $ from 'jquery'
import Backbone from 'backbone'
// eslint-disable-next-line no-unused-vars
import DataTable from 'datatables.net-bs5'
import TableTemplate from './TableTemplate'

export default Backbone.View.extend({
  template: TableTemplate,

  initialize (options = {}) {
    this.collection = options.collection
    this.model = options.model
    this.datastore = options.datastore
    this.metadata = options.metadata
    this.statusValues = new Set()
    this.checksumValues = new Set()
    this.dataTable = null
  },

  render () {
    this.$el.html(this.template())
    this.initializeDataTable()
    this.setupEventListeners()
    return this
  },

  initializeDataTable () {
    this.dataTable = this.$('#filetable').DataTable({
      data: this.prepareTableData(),
      columns: [
        { data: 'file' },
        { data: 'size' },
        { data: 'checksum' },
        { data: 'status' },
        { data: 'actions', orderable: false }
      ],
      rowCallback: (row, data) => this.handleRowCallback(row, data),
      responsive: true,
      dom: "<<'filetable-size'l><'filetable-searchAll d-none'f>>" +
           "<'filetable-content'tr>" +
           "<'filetable-recordInfo d-flex justify-content-center py-3'i>" +
           "<'filetable-pagination d-flex justify-content-center'p>",
      language: {
        search: '_INPUT_',
        searchPlaceholder: 'Search all ...',
        lengthMenu: 'Show _MENU_ per page',
        info: 'Showing _START_ to _END_ of _TOTAL_',
        infoEmpty: 'No files found',
        infoFiltered: '(unfiltered total _MAX_)'
      },
      rowId: 'id',
      initComplete: () => {
        this.updateSelectList()
        $('.dt-search input').css('width', '300px')
      }
    })
  },

  updateOptionValues (valueSet, $filter) {
    const filterOptions = Array.from(valueSet).filter(Boolean).sort()
    $filter.empty()
    $filter.append('<option value="">All</option>')
    filterOptions.forEach(option => {
      $filter.append($('<option>', {
        value: option,
        text: option
      }))
    })
  },

  prepareSingleTableData (model) {
    const fullPath = model.get('path') || ''
    const lastSlashIndex = fullPath.lastIndexOf('/')
    const filename = lastSlashIndex !== -1 ? fullPath.substring(lastSlashIndex + 1) : fullPath
    const checksum = (model.get('hash') || '') !== 'NO_HASH'
      ? `<span title="${model.get('path')}">Yes</span>`
      : 'No'
    const status = model.get('status') || ''
    if (status) this.statusValues.add(status)
    if (checksum) this.checksumValues.add(checksum)

    let actionsHtml = `<div class="dropdown">
                        <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button"
                                data-bs-toggle="dropdown" aria-expanded="false">
                          <i class="fa-solid fa-cog fa-fw"></i>
                          <span class="selected-action"></span>
                        </button>
                        <span class="action-fail btn-icon fa-fw fa-solid fa-exclamation-triangle ms-1" style="display: none;"></span>
                        <span class="spinner-border spinner-border-sm ms-1" role="status" aria-hidden="true" style="display: none;"></span>
                        <ul class="dropdown-menu">
                          <li><a class="dropdown-item text-primary" data-action="Validate" href="#">Validate</a></li>`

    const action = model.get('action')
    const moving = model.get('moving')
    const datastore = model.get('datastore')
    if (datastore === 'dropbox' && moving) {
      actionsHtml += '<li><a class="dropdown-item text-success" data-action="Cancel" href="#">Cancel</a></li>'
    } else if (action === 'move-datastore') {
      actionsHtml += '<li><a class="dropdown-item text-success" data-action="Move to datastore" href="#">Move to datastore</a></li>'
    } else if (action === 'move-metadata') {
      actionsHtml += '<li><a class="dropdown-item text-success" data-action="Move to metadata" href="#">Move to metadata</a></li>'
    } else if (action === 'move-both') {
      actionsHtml += '<li><a class="dropdown-item text-success" data-action="Move to datastore" href="#">Move to datastore</a></li>'
      actionsHtml += '<li><a class="dropdown-item text-success" data-action="Move to metadata" href="#">Move to metadata</a></li>'
    } else if (datastore === 'dropbox' && action === 'accept') {
      actionsHtml += '<li><a class="dropdown-item text-primary" data-action="Accept" href="#">Accept</a></li>'
    } else if (datastore === 'dropbox' && action === 'ignore') {
      actionsHtml += '<li><a class="dropdown-item text-danger" data-action="Ignore" href="#">Ignore</a></li>'
    }
    if (datastore === 'dropbox') {
      actionsHtml += '<li><a class="dropdown-item text-danger" data-action="Delete" href="#">Delete</a></li>'
    }
    actionsHtml += '</ul></div>'

    return {
      file: filename,
      size: model.get('size') || '',
      checksum,
      status,
      actions: actionsHtml,
      id: model.cid,
      model
    }
  },

  prepareTableData () {
    return this.collection.map(model => this.prepareSingleTableData(model))
  },

  handleRowCallback (row, data) {
    data.model.get('open') ? $(row).addClass('table-danger') : $(row).removeClass('table-danger')
  },

  setupEventListeners () {
    // Select search
    this.$('.select-filter').off('change').on('change', (e) => {
      const $select = $(e.target)
      const columnIndex = $select.closest('th').index()
      this.dataTable.column(columnIndex).search($select.val()).draw()
    })

    // Column search
    this.$('.column-search').off('keyup change').on('keyup change', (e) => {
      const $input = $(e.target)
      const $clearBtn = $input.siblings('.clear-search')
      const columnIndex = $input.closest('th').index()
      $clearBtn.toggle(!!$input.val())
      this.dataTable.column(columnIndex).search($input.val()).draw()
    })

    // Clear search
    this.$('.clear-search').off('click').on('click', (e) => {
      const $clearBtn = $(e.target)
      const $input = $clearBtn.siblings('.column-search')
      $input.val('')
      const columnIndex = $input.closest('th').index()
      this.dataTable.column(columnIndex).search('').draw()
      $clearBtn.hide()
    })

    // Table actions
    this.$el.off('click', '.dropdown-item').on('click', '.dropdown-item', (e) => {
      e.preventDefault()
      e.stopPropagation()
      const $actionBtn = $(e.currentTarget)
      const $dropdown = $actionBtn.closest('.dropdown')
      $dropdown.find('.dropdown-menu').removeClass('show')
      const action = $actionBtn.data('action')

      const tr = this.dataTable.row($actionBtn.closest('tr'))
      const rowData = tr.data()
      const model = this.collection.get(rowData.id)

      if (action === 'Validate') {
        this.showInProgress($dropdown, action)
        this.request(
          this,
          $dropdown,
          `${this.model.url()}/${model.get('datastore')}/validate?path=${encodeURIComponent(model.get('path'))}`,
          'POST',
          () => this.getServerState(this, 3000, $dropdown, model, (model) => this.updateTableRow(tr, model))
        )
      } else if (action === 'Accept') {
        this.showInProgress($dropdown, action)
        this.request(
          this,
          $dropdown,
          `${this.model.url()}/${model.get('datastore')}/accept?path=${encodeURIComponent(model.get('path'))}`,
          'POST',
          () => this.getServerState(this, 3000, $dropdown, model, (model) => this.updateTableRow(tr, model))
        )
      } else if (action === 'Cancel') {
        if (window.confirm(`Cancel moving file: ${model.get('path')}?`)) {
          this.showInProgress($dropdown, action)
          this.request(
            this,
            $dropdown,
            `${this.model.url()}/${model.get('datastore')}/cancel?path=${encodeURIComponent(model.get('path'))}`,
            'POST',
            () => this.getServerState(this, 3000, $dropdown, model, () => this.updateTableRow(tr, model))
          )
        }
      } else if (action === 'Delete') {
        if (window.confirm(`Delete file: ${model.get('path')}?`)) {
          this.showInProgress($dropdown, action)
          this.request(
            this,
            $dropdown,
            `${this.model.url()}/${model.get('datastore')}?path=${encodeURIComponent(model.get('path'))}`,
            'DELETE',
            () => {
              this.removeTableRow(tr, model)
              this.addTotal(model.get('datastore'), -1)
            }
          )
        }
      } else if (action === 'Ignore') {
        if (window.confirm(`Ignore file: ${model.get('path')}? This file will be unregistered from the system`)) {
          this.showInProgress($dropdown, action)
          this.request(
            this,
            $dropdown,
            `${this.model.url()}/${model.get('datastore')}/unregister?path=${encodeURIComponent(model.get('path'))}`,
            'POST',
            () => {
              this.removeTableRow(tr, model)
              this.addTotal(model.get('datastore'), -1)
            }
          )
        }
      } else if (action === 'Move to datastore') {
        this.showInProgress($dropdown, action)
        this.request(
          this,
          $dropdown,
          `${this.model.url()}/${model.get('datastore')}/move?path=${encodeURIComponent(model.get('path'))}&to=eidchub`,
          'POST',
          () => {
            const toModel = model.copy('eidchub')
            this.getServerState(this, 3000, $dropdown, toModel, (updatedModel) => {
              this.removeTableRow(tr, model)
              this.datastore.length > 0 ? this.datastore.trigger('addBatch', [updatedModel]) : this.datastore.reset(updatedModel)
              this.addTotal(model.get('datastore'), -1)
              this.addTotal('datastore', 1)
            })
          }
        )
      } else if (action === 'Move to metadata') {
        this.showInProgress($dropdown, action)
        this.request(
          this,
          $dropdown,
          `${this.model.url()}/${model.get('datastore')}/move?path=${encodeURIComponent(model.get('path'))}&to=supporting-documents`,
          'POST',
          () => {
            const toModel = model.copy('supporting-documents')
            this.getServerState(this, 3000, $dropdown, toModel, (updatedModel) => {
              this.removeTableRow(tr, model)
              this.metadata.length > 0 ? this.metadata.trigger('addBatch', [updatedModel]) : this.metadata.reset(updatedModel)
              this.addTotal(model.get('datastore'), -1)
              this.addTotal('metadata', 1)
            })
          }
        )
      }
    })
  },

  addTotal (store, num) {
    let storeTotal = `${store}TotalFiles`
    if (store === 'eidchub') {
      storeTotal = 'datastoreTotalFiles'
    } else if (store === 'supporting-documents') {
      storeTotal = 'metadataTotalFiles'
    }
    this.model.set(storeTotal, (this.model.get(storeTotal) ?? 0) + num)
  },

  updateTableRow (row, model) {
    const rowData = this.prepareSingleTableData(model)
    row.data(rowData).draw(false)

    this.updateSelectList()
  },

  removeTableRow (row, model) {
    this.collection.remove(model)
    row.remove().draw(false)

    this.updateSelectList()
  },

  updateSelectList () {
    this.updateOptionValues(this.statusValues, this.$('.status-filter'))
    this.updateOptionValues(this.checksumValues, this.$('.checksum-filter'))
  },

  showInProgress ($dropdown, action) {
    const $dropdownToggle = $dropdown.find('.dropdown-toggle')
    const $spinner = $dropdown.find('.spinner-border')
    const $failIcon = $dropdown.find('.action-fail')
    const $selectedAction = $dropdown.find('.selected-action')
    $selectedAction.text(action)

    $dropdownToggle.prop('disabled', true)
    $failIcon.hide()
    $spinner.show()
  },

  showNormal ($dropdown) {
    const $dropdownToggle = $dropdown.find('.dropdown-toggle')
    const $spinner = $dropdown.find('.spinner-border')
    const $failIcon = $dropdown.find('.action-fail')
    const $selectedAction = $dropdown.find('.selected-action')
    $selectedAction.text('')

    $dropdownToggle.prop('disabled', false)
    $failIcon.hide()
    $spinner.hide()
  },

  showError ($dropdown) {
    const $dropdownToggle = $dropdown.find('.dropdown-toggle')
    const $spinner = $dropdown.find('.spinner-border')
    const $failIcon = $dropdown.find('.action-fail')

    $dropdownToggle.prop('disabled', false)
    $failIcon.show()
    $spinner.hide()
  },

  request (self, $dropdown, url, method, success) {
    $.ajax({
      url,
      type: method,
      success,
      error: () => self.showError($dropdown)
    })
  },

  getServerState (self, timeout = 0, $dropdown, model, callback) {
    setTimeout(
      $.ajax,
      timeout,
      {
        url: `${self.model.url()}/${model.get('datastore')}?path=${encodeURIComponent(model.get('path'))}`,
        method: 'GET',
        success: (response) => {
          model.update(response.data[0])
          if (callback) callback(model)
        },
        error: () => self.showError($dropdown)
      }
    )
  }
})
