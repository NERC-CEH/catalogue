import $ from 'jquery'
import Backbone from 'backbone'
import { TableView } from '../src/table'
import FileCollection from '../src/File/FileCollection'
import File from '../src/File/File'

describe('TableView', function () {
  let collection
  let view
  const testModels = [
    new File({
      datasetId: '1',
      datastore: 'eidchub',
      path: 'folder1/data1.csv',
      size: '23 KB',
      hash: '28111a4e084f1a57c7a0f50bc9a35472',
      lastValidated: '2022-03-05T06:23:57',
      status: 'INVALID'
    }),
    new File({
      datasetId: '2',
      datastore: 'eidchub',
      path: 'folder2/data2.csv',
      size: '24 MB',
      hash: '28111a4e084f1a57c7a0f50bc9a35472',
      lastValidated: '2022-03-05T06:23:57',
      status: 'VALID'
    })
  ]

  beforeEach(function () {
    document.body.innerHTML = '<div id="test-container"></div>'
    collection = new FileCollection()

    const ViewModel = Backbone.Model.extend({
      url: () => '/upload/123'
    })

    view = new TableView({
      el: '#test-container',
      collection,
      model: new ViewModel(),
      datastore: new FileCollection(),
      metadata: new FileCollection()
    })
  })

  afterEach(function () {
    if (view) view.remove()
    document.body.innerHTML = ''
  })

  describe('initialization', function () {
    it('should initialize with default values', function () {
      expect(view.collection).toBeDefined()
      expect(view.model.url()).toBeDefined()
      expect(view.datastore).toBeDefined()
      expect(view.metadata).toBeDefined()
    })
  })

  describe('table rendering', function () {
    it('should initialize DataTable when view is rendered', function () {
      spyOn($.fn, 'DataTable').and.callThrough()
      view.render()

      expect($.fn.DataTable).toHaveBeenCalled()
      expect(view.dataTable).toBeDefined()
      expect(view.dataTable).not.toBeNull()
      expect($('#filetable').length).toBe(1)
    })
  })

  describe('table data preparation', function () {
    it('should prepare empty table data', function () {
      const data = view.prepareTableData()
      expect(Array.isArray(data)).toBe(true)
      expect(data.length).toBe(0)
    })

    it('should prepare table data from collection', function () {
      collection.reset(testModels)

      const data = view.prepareTableData()
      expect(data.length).toBe(2)
      expect(data[0].file).toBe('data1.csv')
      expect(data[1].file).toBe('data2.csv')
    })
  })

  describe('table search', function () {
    beforeEach(function () {
      collection.reset(testModels)
      view.render()
    })

    it('should filter rows based on column search', function () {
      const fileSearch = view.$('.column-search').first()
      fileSearch.val('data1').trigger('keyup')

      let visibleRows = view.$('#filetable tbody tr:visible')
      expect(visibleRows.length).toBe(1)
      expect(visibleRows.find('td:contains("data1.csv")').length).toBe(1)

      const clearBtn = fileSearch.siblings('.clear-search')
      expect(clearBtn.is(':visible')).toBe(true)

      clearBtn.trigger('click')
      visibleRows = view.$('#filetable tbody tr:visible')
      expect(visibleRows.length).toBe(2)
      expect(clearBtn.is(':visible')).toBe(false)
    })
  })

  describe('table actions', function () {
    let ajaxSpy
    let confirmSpy

    beforeEach(function () {
      collection.reset(testModels)
      view.render()

      ajaxSpy = spyOn($, 'ajax').and.callFake(function (options) {
        if (options.success) {
          options.success({ status: 'PENDING' })
        }
        return $.Deferred().resolve({
          status: 'SUCCESS',
          message: 'Operation completed'
        }).promise()
      })
      spyOn(view, 'getServerState').and.callFake((self, timeout, $dropdown, model, callback) => {
        if (callback) {
          model.set({ status: 'VALID' })
          callback(model)
        }
      })
      confirmSpy = spyOn(window, 'confirm').and.returnValue(true)
    })

    function triggerAction (action) {
      const $actionBtn = view.$('.dropdown-item').first()
      $actionBtn.attr('data-action', action)
      $actionBtn.trigger('click')
      return $actionBtn
    }

    it('should trigger Validate action with correct parameters and get updated state', function () {
      const model = testModels[0]
      triggerAction('Validate')

      expect(ajaxSpy).toHaveBeenCalledWith({
        url: `${view.model.url()}/${model.get('datastore')}/validate?path=${encodeURIComponent(model.get('path'))}`,
        type: 'POST',
        success: jasmine.any(Function),
        error: jasmine.any(Function)
      })

      expect(view.getServerState).toHaveBeenCalledTimes(1)
    })

    it('should trigger Cancel action with confirmation and get updated state', function () {
      const model = testModels[0]
      triggerAction('Cancel')

      expect(confirmSpy).toHaveBeenCalled()
      expect(ajaxSpy).toHaveBeenCalledWith({
        url: `${view.model.url()}/${model.get('datastore')}/cancel?path=${encodeURIComponent(model.get('path'))}`,
        type: 'POST',
        success: jasmine.any(Function),
        error: jasmine.any(Function)
      })

      expect(view.getServerState).toHaveBeenCalledTimes(1)
    })

    it('should trigger Delete action with correct parameters and do not get updated state', function () {
      const model = testModels[0]
      triggerAction('Delete')

      expect(confirmSpy).toHaveBeenCalled()
      expect(ajaxSpy).toHaveBeenCalledWith({
        url: `${view.model.url()}/${model.get('datastore')}?path=${encodeURIComponent(model.get('path'))}`,
        type: 'DELETE',
        success: jasmine.any(Function),
        error: jasmine.any(Function)
      })

      expect(view.getServerState).toHaveBeenCalledTimes(0)
    })
  })
})
