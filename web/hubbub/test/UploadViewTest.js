import $ from 'jquery'
import { UploadModel, UploadView } from '../src/Upload'

describe('UploadView', function () {
  const id = 'ebaf006d-b014-4d8b-9131-7677e61519dd'
  let model
  let view

  describe('initializing', function () {
    beforeEach(function () {
      $(document.body).empty()
    })
    it('empty DOM', function () {
      // given
      // when
      view = new UploadView({ id })

      // then
      expect($('.dropzone-container').length).toEqual(0)
      expect(view.dropbox.length).toEqual(0)
      expect(view.datastore.length).toEqual(0)
      expect(view.metadata.length).toEqual(0)
    })
    it('full DOM', function () {
      // given
      $(document.body).empty()
      $(document.body)
        .append(`
            <div class="document-upload">
                <div class="dropbox-files"></div>
                <div class="data-files"></div>
                <div class="metadata-files"></div>
            </div>
            <script id="dropbox-data" type="application/json">
                {"data":[{"bytes":18,"datasetId":"${id}","datastore":"dropbox","format":"csv","path":"data1.csv","status":"WRITING"},{"bytes":732,"datasetId":"${id}","datastore":"dropbox","format":"csv","path":"data2.csv","status":"WRITING"},{"bytes":981,"datasetId":"${id}","datastore":"dropbox","format":"csv","path":"data3.csv","status":"WRITING"}],"links":{"self":"http://hubbub:8080/v7/${id}/dropbox?page=1&size=20"},"meta":{"currentPage":1,"lastPage":1,"pageSize":20}}
            </script>
            <script id="datastore-data" type="application/json">
                {"data":[{"bytes":532,"datasetId":"${id}","datastore":"eidchub","format":"csv","hash":"5a000e6b27f42ad444721c1feeb77818","hashingTime":0.23,"lastModified":"2022-01-13T16:45:28","lastValidated":"2022-03-02T23:09:25","path":"data4.csv","status":"VALID"},{"bytes":9342,"datasetId":"${id}","datastore":"eidchub","format":"csv","hash":"32084c82ce91d79545871a083b575b91","hashingTime":0.17,"lastModified":"2007-12-06T05:50:15","lastValidated":"2022-03-01T01:29:54","path":"data5.csv","status":"VALID"}],"links":{"self":"http://hubbub:8080/v7/${id}/eidchub?page=1&size=20"},"meta":{"currentPage":1,"lastPage":1,"pageSize":20}}
            </script>
            <script id="metadata-data" type="application/json">
                {"data":[{"bytes":28212,"datasetId":"${id}","datastore":"supporting-documents","format":"pdf","hash":"b88c85c371c7c6566f7ff36071ef324d","hashingTime":1.47,"lastModified":"2020-07-23T06:58:37","lastValidated":"2021-12-26T19:47:48","path":"support.pdf","status":"VALID"}],"links":{"self":"http://hubbub:8080/v7/${id}/supporting-documents?page=1&size=20"},"meta":{"currentPage":1,"lastPage":1,"pageSize":20}}
            </script>
        `)

      // when
      view = new UploadView({ id })

      // then
      expect($('.dropzone-container').length).toEqual(1)
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

  beforeEach(function () {
    model = new UploadModel({ id })
    view = new UploadView({ model })
  })

  it('is defined', function () {
    // then
    expect(view).toBeDefined()
  })

  xit('loadDatastore should be triggered', () => {
    // given
    view.render()
    view.delegateEvents()
    spyOn($, 'ajax').and.callFake(function (e) {
      return ''
    })
    spyOn(view, 'loadDatastore')

    // when
    view.$('.load.datastore').trigger('click')

    // then
    setTimeout(function () {
      expect(view.loadDatastore).toHaveBeenCalled()
    })
  })

  it('loadDropbox should be triggered', () => {
    // given
    view.render()
    view.delegateEvents()
    spyOn($, 'ajax').and.callFake(function (e) {
      return ''
    })
    spyOn(view, 'loadDropbox')

    // when
    view.$('.load.data').trigger('click')

    // then
    setTimeout(function () {
      expect(view.loadDropbox).toHaveBeenCalled()
    })
  })

  xit('loadMetadata should be triggered', () => {
    // given
    view.render()
    view.delegateEvents()
    spyOn($, 'ajax').and.callFake(function (e) {
      return ''
    })
    spyOn(view, 'loadMetadata')

    // when
    view.$('.load.metadata').trigger('click')

    // then
    setTimeout(function () {
      expect(view.loadMetadata).toHaveBeenCalled()
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
