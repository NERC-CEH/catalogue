import $ from 'jquery'
import { UploadModel, UploadView } from '../../src/Upload'
import FileCollection from '../../src/File/FileCollection'

describe('UploadView', function () {
  const id = 'ebaf006d-b014-4d8b-9131-7677e61519dd'
  let model
  let view
  const testResponses = {
    success: {
      status: 204
    },
    error: {
      status: 500
    },
    serverState: {
      status: 200,
      responseText: '{"data":[{},{}],"meta":{"currentPage":7}}'
    }
  }

  describe('initializing', function () {
    beforeEach(function () {
      model = new UploadModel({ id })
    })

    it('empty DOM', function () {
      // given
      $(document.body).empty()

      // when
      view = new UploadView({
        el: '.document-upload',
        model
      })

      // then
      expect($('.dropzone-container').length).toEqual(0)
      expect(view.dropbox.length).toEqual(0)
      expect(view.datastore.length).toEqual(0)
      expect(view.metadata.length).toEqual(0)
    })

    it('full DOM', function () {
      // given
      $(document.body)
        .html(`
            <div class="document-upload">
                <div class="dropzone-container">
                    <div class="dropzone-files"><div>
                    <button class="fileinput-button"></button>    
                </div>
                <div class="datastore-files"></div>
                <div class="data-files"></div>
                <div class="metadata-files"></div>
            </div>
            <script id="dropbox-data" type="application/json">
                {
                    "data":[
                      {"bytes":18,"datasetId":"${id}","datastore":"dropbox","format":"csv","path":"data1.csv","status":"WRITING"},
                      {"bytes":732,"datasetId":"${id}","datastore":"dropbox","format":"csv","path":"data2.csv","status":"WRITING"},
                      {"bytes":981,"datasetId":"${id}","datastore":"dropbox","format":"csv","path":"data3.csv","status":"WRITING"}
                    ],
                    "links":{
                        "self":"http://hubbub:8080/v7/${id}/dropbox?page=1&size=20"
                      },
                    "meta":{
                        "currentPage":1,
                        "lastPage":1,
                        "pageSize":20
                    }
                }
            </script>
            <script id="datastore-data" type="application/json">
                {
                    "data":[
                        {"bytes":532,"datasetId":"${id}","datastore":"eidchub","format":"csv","hash":"5a000e6b27f42ad444721c1feeb77818","hashingTime":0.23,"lastModified":"2022-01-13T16:45:28","lastValidated":"2022-03-02T23:09:25","path":"data4.csv","status":"VALID"},
                        {"bytes":9342,"datasetId":"${id}","datastore":"eidchub","format":"csv","hash":"32084c82ce91d79545871a083b575b91","hashingTime":0.17,"lastModified":"2007-12-06T05:50:15","lastValidated":"2022-03-01T01:29:54","path":"data5.csv","status":"VALID"}
                      ],
                      "links":{
                        "self":"http://hubbub:8080/v7/${id}/eidchub?page=1&size=20"
                      },
                      "meta":{
                        "currentPage":1,
                        "lastPage":1,
                        "pageSize":20
                      }
                    }
            </script>
            <script id="metadata-data" type="application/json">
                {
                    "data":[
                        {"bytes":28212,"datasetId":"${id}","datastore":"supporting-documents","format":"pdf","hash":"b88c85c371c7c6566f7ff36071ef324d","hashingTime":1.47,"lastModified":"2020-07-23T06:58:37","lastValidated":"2021-12-26T19:47:48","path":"support.pdf","status":"VALID"}
                      ],
                    "links":{
                        "self":"http://hubbub:8080/v7/${id}/supporting-documents?page=1&size=20"
                    },
                    "meta":{
                        "currentPage":1,
                        "lastPage":1,
                        "pageSize":20
                    }
                }
            </script>
        `)

      // when
      view = new UploadView({
        el: '.document-upload',
        model
      })

      // then
      expect(view.model).toBeDefined()
      expect($('.dropzone-container').length).toEqual(1)
      expect(view.$dropbox.length).toEqual(1)
      expect(view.$datastore.length).toEqual(1)
      expect(view.$metadata.length).toEqual(1)
      expect(view.dropbox.length).toEqual(3)
      expect(view.datastore.length).toEqual(2)
      expect(view.metadata.length).toEqual(1)
      expect(view.model.get('dropboxPage')).toEqual(1)
      expect(view.model.get('dropboxSize')).toEqual(20)
      expect(view.model.get('dropboxLastPage')).toEqual(1)
      expect(view.model.get('datastorePage')).toEqual(1)
      expect(view.model.get('datastoreSize')).toEqual(20)
      expect(view.model.get('datastoreLastPage')).toEqual(1)
      expect(view.model.get('metadataPage')).toEqual(1)
      expect(view.model.get('metadataSize')).toEqual(20)
      expect(view.model.get('metadataLastPage')).toEqual(1)
    })
  })

  describe('load more files', function () {
    beforeEach(function () {
      $(document.body)
        .html(`
            <div class="document-upload">
                <div class="datastore-files"></div>
                <div class="data-files"></div>
                <div class="metadata-files"></div>
                <button class="load datastore"></button>
                <button class="load data"></button>
                <button class="load metadata"></button>
            </div>
        `)
      model = new UploadModel({ id })
      view = new UploadView({
        el: '.document-upload',
        model
      })
    })

    it('trigger loadDatastore', function () {
      // given
      const $button = view.$('.load.datastore')
      spyOn(view, 'loadMore')

      // when
      $button.trigger('click')

      // then
      expect($button.length).toEqual(1)
      expect(view.loadMore).toHaveBeenCalledWith(jasmine.anything(), 'datastore', 'eidchub', jasmine.anything())
    })

    it('trigger loadDropbox', function () {
      // given
      const $button = view.$('.load.data')
      spyOn(view, 'loadMore')

      // when
      $button.trigger('click')

      // then
      expect($button.length).toEqual(1)
      expect(view.loadMore).toHaveBeenCalledWith(jasmine.anything(), 'dropbox', 'dropbox', jasmine.anything())
    })

    it('trigger loadMetadata', function () {
      // given
      const $button = view.$('.load.metadata')
      spyOn(view, 'loadMore')

      // when
      $button.trigger('click')

      // then
      expect($button.length).toEqual(1)
      expect(view.loadMore).toHaveBeenCalledWith(jasmine.anything(), 'metadata', 'supporting-documents', jasmine.anything())
    })
  })

  describe('loadMore', function () {
    beforeEach(function () {
      jasmine.Ajax.install()
      $(document.body)
        .html(`
            <div class="document-upload">
                <div class="datastore-files"></div>
                <div class="data-files"></div>
                <div class="metadata-files"></div>
            </div>
        `)
      model = new UploadModel({
        id,
        datastorePage: 6,
        datastoreSize: 3,
        datastoreLastPage: 8,
        metadataPage: 1,
        metadataSize: 10,
        metadataLAstPage: 1
      })
      view = new UploadView({
        el: '.document-upload',
        model
      })
    })

    afterEach(function () {
      jasmine.Ajax.uninstall()
    })

    it('when next page is available', function () {
      // given
      const event = {}
      const collection = new FileCollection()
      const testResponses = {
        success: {
          status: 200,
          responseText: '{"data":[{},{}],"meta":{"currentPage":7}}'
        }
      }
      spyOn(view, 'showInProgress')

      // when
      view.loadMore(event, 'datastore', 'eidchub', collection)

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.success)

      expect(request.method).toBe('GET')
      expect(request.url).toBe(`/upload/${id}/eidchub?page=7&size=3`)
      expect(collection.length).toEqual(2)
      expect(view.showInProgress).toHaveBeenCalledWith(event)
      expect(view.model.get('datastorePage')).toEqual(7)
    })

    it('already on last page', function () {
      // given
      const event = {}
      const collection = new FileCollection()
      spyOn(view, 'showInProgress')
      spyOn($, 'ajax')

      // when
      view.loadMore(event, 'metadata', 'supporting-documents', collection)

      // then
      expect(view.showInProgress).not.toHaveBeenCalled()
      expect($.ajax).not.toHaveBeenCalled()
    })
  })

  describe('finish', function () {
    beforeEach(function () {
      jasmine.Ajax.install()
      $(document.body)
        .html(`
            <div class="document-upload">
                <button class="finish"></button>
            </div>
        `)
      model = new UploadModel({ id })
      view = new UploadView({
        el: '.document-upload',
        model
      })
    })

    afterEach(function () {
      jasmine.Ajax.uninstall()
    })

    it('success', function () {
      // given
      const $finishBtn = view.$('.finish')
      spyOn(view, 'showInProgress')
      spyOn(view, 'showNormal')
      spyOn(view, 'redirectToDocuments')

      // when
      $finishBtn.trigger('click')

      // then
      expect($finishBtn.length).toEqual(1)

      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.success)

      expect(request.method).toBe('POST')
      expect(request.url).toBe(`/upload/${id}/finish`)
      expect(request.status).toBe(204)
      expect(view.showInProgress).toHaveBeenCalled()
      expect(view.showNormal).toHaveBeenCalled()
      expect(view.redirectToDocuments).toHaveBeenCalled()
    })

    it('error', function () {
      // given
      const $finish = view.$('.finish')
      spyOn(view, 'showInProgress')
      spyOn(view, 'showNormal')
      spyOn(view, 'showInError')

      // when
      $finish.trigger('click')

      // then
      expect($finish.length).toEqual(1)

      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.error)

      expect(request.method).toBe('POST')
      expect(request.url).toBe(`/upload/${id}/finish`)
      expect(view.showInProgress).toHaveBeenCalled()
      expect(view.showNormal).not.toHaveBeenCalled()
      expect(view.showInError).toHaveBeenCalled()
    })
  })

  describe('move', function () {
    beforeEach(function () {
      jasmine.Ajax.install()
      $(document.body)
        .html(`
            <div class="document-upload">
                <button class="move-all"></button>
            </div>
        `)
      model = new UploadModel({ id })
      view = new UploadView({
        el: '.document-upload',
        model
      })
    })

    afterEach(function () {
      jasmine.Ajax.uninstall()
    })

    it('success', function () {
      // given
      spyOn(view, 'showInProgress')
      spyOn(view, 'showNormal')
      spyOn(view.dropbox, 'each')
      spyOn(view.dropbox, 'reset')

      // when
      view.$('.move-all').trigger('click')

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.serverState)

      expect(request.method).toBe('POST')
      expect(request.url).toBe(`/upload/${id}/dropbox/move?to=eidchub`)
      expect(view.showInProgress).toHaveBeenCalled()
      expect(view.showNormal).toHaveBeenCalled()
      expect(view.dropbox.each).toHaveBeenCalled()
      expect(view.dropbox.reset).toHaveBeenCalled()
    })
  })

  describe('scheduling', function () {
    beforeEach(function () {
      jasmine.Ajax.install()
      $(document.body)
        .html(`
            <div class="document-upload">
                <button class="schedule"></button>
                <button class="reschedule"></button>
            </div>
        `)
      model = new UploadModel({
        id,
        datastoreSize: 20
      })
      view = new UploadView({
        el: '.document-upload',
        model
      })
    })

    afterEach(function () {
      jasmine.Ajax.uninstall()
    })

    it('trigger reschedule', function () {
      // given
      spyOn(view, 'showInProgress')
      spyOn(view, 'showNormal')
      spyOn(view, 'reloadPage')

      // when
      view.$('.reschedule').trigger('click')

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.success)

      expect(request.method).toBe('POST')
      expect(request.url).toBe(`/upload/${id}/reschedule`)
      expect(view.showInProgress).toHaveBeenCalled()
      expect(view.showNormal).toHaveBeenCalled()
      expect(view.reloadPage).toHaveBeenCalled()
    })

    it('trigger schedule', function () {
      // given
      spyOn(view, 'showInProgress')
      spyOn(view, 'showNormal')
      spyOn(view, 'reloadPage')

      // when
      view.$('.schedule').trigger('click')

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.success)

      expect(request.method).toBe('POST')
      expect(request.url).toBe(`/upload/${id}/schedule`)
      expect(view.showInProgress).toHaveBeenCalled()
      expect(view.showNormal).toHaveBeenCalled()
      expect(view.reloadPage).toHaveBeenCalled()
    })

    it('error', function () {
      // given
      spyOn(view, 'showInProgress')
      spyOn(view, 'showNormal')
      spyOn(view, 'reloadPage')
      spyOn(view, 'showInError')

      // when
      view.$('.schedule').trigger('click')

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.error)

      expect(request.method).toBe('POST')
      expect(request.url).toBe(`/upload/${id}/schedule`)
      expect(view.showInProgress).toHaveBeenCalled()
      expect(view.showNormal).not.toHaveBeenCalled()
      expect(view.reloadPage).not.toHaveBeenCalled()
      expect(view.showInError).toHaveBeenCalled()
    })
  })

  describe('validate', function () {
    beforeEach(function () {
      jasmine.Ajax.install()
      $(document.body)
        .html(`
            <div class="document-upload">
                <button class="validate-all"></button>
            </div>
        `)
      model = new UploadModel({ id })
      view = new UploadView({
        el: '.document-upload',
        model
      })
    })

    afterEach(function () {
      jasmine.Ajax.uninstall()
    })

    it('validate-all', function () {
      // given
      spyOn(view, 'validate')

      // when
      view.$('.validate-all').trigger('click')

      // then
      expect(view.validate).toHaveBeenCalledTimes(3)
    })

    it('success', function () {
      // given
      const collection = new FileCollection()
      const event = {}
      spyOn(view, 'collectionSuccess')

      // when
      view.validate(event, 'datastore', 'eidchub', collection)

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.success)
      expect(request.method).toBe('POST')
      expect(request.url).toBe(`/upload/${id}/eidchub/validate`)
      expect(view.collectionSuccess).toHaveBeenCalledWith(event, undefined, 'datastore', 'eidchub', collection)
    })

    it('getServerState', function () {
      // given
      const collection = new FileCollection()
      spyOn(collection, 'reset')
      const callback = jasmine.createSpy('callback')
      // when
      view.getServerState('datastore', 'eidchub', collection, callback)
      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.serverState)
      expect(request.method).toBe('GET')
      expect(request.url).toBe(`/upload/${id}/eidchub?page=1&size=20`)
      expect(collection.reset).toHaveBeenCalledWith([{}, {}])
      expect(callback).toHaveBeenCalled()
    })
  })

  describe('hash', function () {
    beforeEach(function () {
      jasmine.Ajax.install()
      $(document.body)
        .html(`
            <div class="document-upload">
                <button class="hash-dropbox"></button>
            </div>
        `)
      model = new UploadModel({ id })
      view = new UploadView({
        el: '.document-upload',
        model
      })
    })

    afterEach(function () {
      jasmine.Ajax.uninstall()
    })

    it('success', function () {
      // given
      spyOn(view, 'collectionSuccess')

      // when
      view.$('.hash-dropbox').trigger('click')

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.success)
      expect(request.method).toBe('POST')
      expect(request.url).toBe(`/upload/${id}/hash`)
      expect(view.collectionSuccess).toHaveBeenCalledWith(jasmine.anything(), undefined, 'dropbox', 'dropbox', jasmine.anything())
    })
  })

  describe('register', function () {
    beforeEach(function () {
      jasmine.Ajax.install()
      $(document.body)
        .html(`
            <div class="document-upload">
                <button class="register-dropbox"></button>
            </div>
        `)
      model = new UploadModel({ id })
      view = new UploadView({
        el: '.document-upload',
        model
      })
    })

    afterEach(function () {
      jasmine.Ajax.uninstall()
    })

    it('success', function () {
      // given
      spyOn(view, 'showInProgress')
      spyOn(view, 'showNormal')
      spyOn(view, 'reloadPage')

      // when
      view.$('.register-dropbox').trigger('click')

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.success)
      expect(request.method).toBe('POST')
      expect(request.url).toBe(`/upload/${id}/register`)
      expect(view.showInProgress).toHaveBeenCalled()
      expect(view.showNormal).toHaveBeenCalled()
      expect(view.reloadPage).toHaveBeenCalled()
    })
  })
})
