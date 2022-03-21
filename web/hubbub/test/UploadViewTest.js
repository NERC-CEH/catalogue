import $ from 'jquery'
import { UploadModel, UploadView } from '../src/Upload'
import FileCollection from '../src/File/FileCollection'

describe('UploadView', function () {
  const id = 'ebaf006d-b014-4d8b-9131-7677e61519dd'
  let model
  let view

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
      const TestResponses = {
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
      request.respondWith(TestResponses.success)

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
    const testResponses = {
      success: {
        status: 302,
        responseHeaders: {
          location: `/documents/${id}`
        }
      },
      error: {
        status: 500
      }
    }

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
      const $finish = view.$('.finish')

      // when
      $finish.trigger('click')

      // then
      expect($finish.length).toEqual(1)

      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.success)

      expect(request.method).toBe('POST')
      expect(request.url).toBe(`/upload/${id}/finish`)
      expect(request.status).toBe(302)
      expect(request.getResponseHeader('location')).toBe(`/documents/${id}`)
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

  xit('moveAllDatastore should be triggered', () => {
    // given
    view.render()
    view.delegateEvents()
    spyOn($, 'ajax').and.callFake(function (e) {
      return ''
    })
    spyOn(view, 'loadMetadata')

    // when
    view.$('.move-all').trigger('click')

    // then
    setTimeout(function () {
      expect(view.moveAllDatastore).toHaveBeenCalled()
    })
  })

  xit('reschedule should be triggered', () => {
    // given
    view.render()
    view.delegateEvents()
    spyOn($, 'ajax').and.callFake(function (e) {
      return ''
    })
    spyOn(view, 'reschedule')

    // when
    view.$('.reschedule').trigger('click')

    // then
    setTimeout(function () {
      expect(view.reschedule).toHaveBeenCalled()
    })
  })

  xit('schedule should be triggered', () => {
    // given
    view.render()
    view.delegateEvents()
    spyOn($, 'ajax').and.callFake(function (e) {
      return ''
    })
    spyOn(view, 'schedule')

    // when
    view.$('.schedule').trigger('click')

    // then
    setTimeout(function () {
      expect(view.schedule).toHaveBeenCalled()
    })
  })

  xit('validate-all should be triggered', () => {
    // given
    view.render()
    view.delegateEvents()
    spyOn($, 'ajax').and.callFake(function (e) {
      return ''
    })
    spyOn(view, 'validateAll')

    // when
    view.$('.validate-all').trigger('click')

    // then
    setTimeout(function () {
      expect(view.validateAll).toHaveBeenCalled()
    })
  })
})
