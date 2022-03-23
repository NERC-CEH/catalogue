import $ from 'jquery'
import { File, FileCollection, FileView } from '../../src/File'

describe('FileView', function () {
  const id = '658bc1ee-c1f9-42fd-ab94-fdd2909259c6'
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

    view = new FileView({
      collection: new FileCollection(),
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

  xit('moveDatastore should be triggered', function () {
    // given
    view.render()
    view.delegateEvents()
    spyOn($, 'ajax').and.callFake(function (e) {
      return ''
    })
    spyOn(view, 'moveDatastore')

    // when
    view.$('.move-datastore').trigger('click')

    // then
    setTimeout(function () {
      expect(view.moveDatastore).toHaveBeenCalled()
    })
  })

  xit('moveDatastore should be triggered', function () {
    // given
    view.render()
    view.delegateEvents()
    spyOn($, 'ajax').and.callFake(function (e) {
      return ''
    })
    spyOn(view, 'moveMetadata')

    // when
    view.$('.move-metadata').trigger('click')

    // then
    setTimeout(function () {
      expect(view.moveMetadata).toHaveBeenCalled()
    })
  })

  xit('showDelete should be triggered', function () {
    // given
    view.render()
    view.delegateEvents()
    spyOn(view, 'showDelete')
    model.set({ action: 'move-both' })

    // when
    view.$('.delete').trigger('click')

    // then

    expect(view.showDelete).toHaveBeenCalled()
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

  xit('showCancel should be triggered', function () {
    // given
    view.render()
    view.delegateEvents()
    spyOn(view, 'showCancel')
    model.set({ action: 'moving' })

    // when
    view.$('.cancel').trigger('click')

    // then

    expect(view.showCancel).toHaveBeenCalled()
  })

  describe('accept', function () {
    it('should be triggered', function () {
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

    it('success', function () {
      // given
      const event = {}
      spyOn(view, 'showInProgress')
      spyOn(view, 'getServerState')

      // when
      view.accept(event)

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.success)

      expect(request.method).toBe('POST')
      expect(request.url).toBe(`/upload/${id}/eidchub/accept?path=data.csv`)

      expect(view.showInProgress).toHaveBeenCalledWith(event)
      // noinspection JSCheckFunctionSignatures
      expect(view.getServerState).toHaveBeenCalledWith(3000, jasmine.any(Function))
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
