import $ from 'jquery'
import { File, FileCollection, FileView } from '../../src/File'

describe('FileView', function () {
  const id = '658bc1ee-c1f9-42fd-ab94-fdd2909259c6'
  let model
  let view
  let collection
  const testResponses = {
    success: {
      status: 204
    },
    error: {
      status: 500
    },
    serverState: {
      status: 200,
      responseText: `{"data":[{"bytes":532,"datasetId":"${id}","datastore":"eidchub","format":"csv","hash":"5a000e6b27f42ad444721c1feeb77818","hashingTime":0.23,"lastModified":"2022-01-13T16:45:28","lastValidated":"2022-03-02T23:09:25","path":"data4.csv","status":"VALID"}],"meta":{"currentPage":1}}`
    }
  }

  beforeEach(function () {
    jasmine.Ajax.install()
    model = new File({
      datasetId: id,
      datastore: 'eidchub',
      path: 'data.csv',
      size: 23,
      hash: '28111a4e084f1a57c7a0f50bc9a35472',
      lastValidated: '2022-03-05T06:23:57',
      status: 'VALID'
    })

    collection = new FileCollection([model])

    view = new FileView({
      collection,
      datastore: new FileCollection(),
      metadata: new FileCollection(),
      model,
      url: `/upload/${id}`
    })
  })

  afterEach(function () {
    jasmine.Ajax.uninstall()
  })

  describe('initialize', function () {
    it('no check', function () {
      // given

      // when
      view.render()

      // then
      expect(view).toBeDefined()
      expect(view.url).toBe(`/upload/${id}`)
    })

    it('with check', function () {
      // given
      model.set('check', true)
      spyOn(view, 'getServerState')

      // when
      view.initialize({
        collection: new FileCollection(),
        datastore: new FileCollection(),
        metadata: new FileCollection(),
        model,
        url: `/upload/${id}`
      })

      // then
      expect(view.getServerState).toHaveBeenCalledWith(7000)
    })
  })

  describe('getServerState', function () {
    it('success', function () {
      // given
      const callback = jasmine.createSpy('callback')
      spyOn(view.model, 'update')

      // when
      view.getServerState(0, callback)

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.serverState)

      expect(request.method).toBe('GET')
      expect(request.url).toBe(`/upload/${id}/eidchub?path=data.csv`)

      expect(callback).toHaveBeenCalled()
      expect(view.model.update).toHaveBeenCalled()
    })

    it('error', function () {
      // given
      const callback = jasmine.createSpy('callback')

      // when
      view.getServerState(0, callback)

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.error)

      expect(request.method).toBe('GET')
      expect(request.url).toBe(`/upload/${id}/eidchub?path=data.csv`)

      expect(callback).not.toHaveBeenCalled()
    })
  })

  xit('validate should be triggered', function () {
    // given
    view.render()
    view.delegateEvents()
    spyOn($, 'ajax').and.callFake(function (e) {
      return ''
    })
    spyOn(view, 'validate')

    // when
    view.$('.validate').trigger('click')

    // then
    setTimeout(function () {
      expect(view.validate).toHaveBeenCalled()
    })
  })

  describe('move', function () {
    it('datastore trigger', function () {
      // given
      model.set({ action: 'move-datastore' })
      view.render()
      const $moveBtn = view.$('.move-datastore')
      spyOn(view, 'moveDatastore')
      view.delegateEvents()

      // when
      $moveBtn.trigger('click')

      // then
      expect($moveBtn).toHaveSize(1)
      expect(view.moveDatastore).toHaveBeenCalled()
    })

    it('metadata trigger', function () {
      // given
      model.set({ action: 'move-metadata' })
      view.render()
      const $moveBtn = view.$('.move-metadata')
      spyOn(view, 'moveMetadata')
      view.delegateEvents()

      // when
      $moveBtn.trigger('click')

      // then
      expect($moveBtn).toHaveSize(1)
      expect(view.moveMetadata).toHaveBeenCalled()
    })

    it('success', function () {
      // given
      model.set({ datastore: 'dropbox' })
      const event = {}
      spyOn(view, 'showInProgress')
      spyOn(view, 'remove')

      // when
      view.move(event, 'eidchub')

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.success)

      expect(request.method).toBe('POST')
      expect(request.url).toBe(`/upload/${id}/dropbox/move?path=data.csv&to=eidchub`)
      expect(view.showInProgress).toHaveBeenCalled()
      expect(view.remove).toHaveBeenCalled()
      expect(collection).toHaveSize(0)
      expect(view.datastore).toHaveSize(1)
    })
  })

  describe('delete', function () {
    it('trigger', function () {
      // given
      model.set({ action: 'move-both' })
      view.render()
      const $deleteBtn = view.$('.delete')
      spyOn(view, 'showDelete')
      view.delegateEvents()

      // when
      $deleteBtn.trigger('click')

      // then
      expect($deleteBtn).toHaveSize(1)
      expect(view.showDelete).toHaveBeenCalled()
    })

    it('showDelete', function () {
      // given
      const event = {}
      spyOn(view, 'showConfirm')

      // when
      view.showDelete(event)

      // then
      expect(view.showConfirm).toHaveBeenCalledWith(
        jasmine.anything(),
        'Delete file: data.csv?',
        view.delete
      )
    })

    it('success', function () {
      // given
      const event = {}
      spyOn(view, 'remove')
      spyOn(collection, 'remove')

      // when
      view.delete(event)

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.success)

      expect(request.method).toBe('DELETE')
      expect(request.url).toBe(`/upload/${id}/eidchub?path=data.csv`)

      expect(view.remove).toHaveBeenCalled()
      expect(collection.remove).toHaveBeenCalled()
    })
  })

  xit('showIgnore should be triggered', function () {
    // given
    view.render()
    view.delegateEvents()
    spyOn(view, 'showIgnore')
    model.set({ action: 'ignore' })

    // when
    view.$('.ignore').trigger('click')

    // then

    expect(view.showIgnore).toHaveBeenCalled()
  })

  describe('cancel', function () {
    it('trigger', function () {
      // given
      model.set({ moving: true })
      view.render()
      const $cancelBtn = view.$('.cancel')
      spyOn(view, 'showCancel')
      view.delegateEvents()

      // when
      $cancelBtn.trigger('click')

      // then
      expect($cancelBtn).toHaveSize(1)
      expect(view.showCancel).toHaveBeenCalled()
    })

    it('showCancel', function () {
      // given
      const event = {}
      spyOn(view, 'showConfirm')

      // when
      view.showCancel(event)

      // then
      expect(view.showConfirm).toHaveBeenCalledWith(
        jasmine.anything(),
        'Cancel moving file: data.csv?',
        view.cancel
      )
    })

    it('request', function () {
      // given
      const event = {}
      spyOn(view, 'request')

      // when
      view.cancel(event)

      // then
      // noinspection JSCheckFunctionSignatures
      expect(view.request).toHaveBeenCalledWith(
        jasmine.anything(),
        `/upload/${id}/eidchub/cancel?path=data.csv`,
        'POST',
        jasmine.any(Function)
      )
    })
  })

  describe('accept', function () {
    it('trigger', function () {
      // given
      model.set({ action: 'accept' })
      view.render()
      const $acceptBtn = view.$('.accept')
      spyOn(view, 'accept')
      view.delegateEvents()

      // when
      $acceptBtn.trigger('click')

      // then
      expect($acceptBtn).toHaveSize(1)
      expect(view.accept).toHaveBeenCalled()
    })

    it('request', function () {
      // given
      const event = {}
      spyOn(view, 'showInProgress')
      spyOn(view, 'request')

      // when
      view.accept(event)

      // then
      expect(view.showInProgress).toHaveBeenCalledWith(event)
      // noinspection JSCheckFunctionSignatures
      expect(view.request).toHaveBeenCalledWith(
        event,
        `/upload/${id}/eidchub/accept?path=data.csv`,
        'POST',
        jasmine.any(Function)
      )
    })

    it('error', function () {
      // given
      const event = {}
      spyOn(view, 'showInError')

      // when
      view.accept(event)

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.error)

      expect(request.method).toBe('POST')
      expect(request.url).toBe(`/upload/${id}/eidchub/accept?path=data.csv`)
      expect(view.showInError).toHaveBeenCalledWith(event)
    })
  })

  it('expand should be triggered', function () {
    // given
    view.render()
    const $panelHeading = view.$('.panel-heading')
    spyOn(view, 'expand')
    view.delegateEvents()

    // when
    $panelHeading.trigger('click')

    // then
    expect($panelHeading).toHaveSize(1)
    expect(view.expand).toHaveBeenCalled()
  })
})
