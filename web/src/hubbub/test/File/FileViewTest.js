import { File, FileCollection, FileView } from '../../src/File'

describe('FileView', function () {
  const id = '658bc1ee-c1f9-42fd-ab94-fdd2909259c6'
  let model
  let view
  let collection
  const response = {
    data: [
      { bytes: 532, datasetId: id, datastore: 'eidchub', format: 'csv', hash: '5a000e6b27f42ad444721c1feeb77818', hashingTime: 0.23, lastModified: '2022-01-13T16:45:28', lastValidated: '2022-03-02T23:09:25', path: 'data4.csv', status: 'VALID' }
    ],
    meta: { currentPage: 1 }
  }
  const testResponses = {
    success: {
      status: 204
    },
    error: {
      status: 500
    },
    serverState: {
      status: 200,
      responseText: JSON.stringify(response)
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
      expect(view.el).toBeDefined()
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
      expect(view.getServerState).toHaveBeenCalledWith(
        view,
        3000
      )
    })
  })

  describe('getServerState', function () {
    xit('request', function () {
      // given
      const callback = jasmine.createSpy('callback')

      // when
      view.getServerState(view, 0, callback)

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.serverState)
      expect(request.url).toBe(`/upload/${id}/eidchub?path=data.csv`)
      expect(request.method).toBe('GET')
    })

    xit('error', function () {
      // given
      const callback = () => {}
      spyOn(view, 'showInError')

      // when
      view.getServerState(view, 0, callback)

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.error)

      expect(request.method).toBe('GET')
      expect(request.url).toBe(`/upload/${id}/eidchub?path=data.csv`)
      expect(view.showInError).toHaveBeenCalled()
    })
  })

  describe('validate', function () {
    it('trigger', function () {
      // given
      model.set({ action: 'validate' })
      view.render()
      const $validateBtn = view.$('.validate')
      spyOn(view, 'validate')
      view.delegateEvents()

      // when
      $validateBtn.trigger('click')

      // then
      expect($validateBtn).toHaveSize(1)
      expect(view.validate).toHaveBeenCalled()
    })

    it('request', function () {
      // given
      const event = new Event('click')
      spyOn(view, 'request')

      // when
      view.validate(event)

      // then
      // noinspection JSCheckFunctionSignatures
      expect(view.request).toHaveBeenCalledWith(
        jasmine.anything(),
        event,
                `/upload/${id}/eidchub/validate?path=data.csv`,
                'POST',
                jasmine.any(Function)
      )
    })
  })

  describe('move', function () {
    it('trigger - datastore', function () {
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

    it('trigger - metadata', function () {
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

    it('request', function () {
      // given
      model.set({ datastore: 'dropbox' })
      const event = new Event('click')
      spyOn(view, 'request')

      // when
      view.move(event, 'eidchub')

      // then
      // noinspection JSCheckFunctionSignatures
      expect(view.request).toHaveBeenCalledWith(
        jasmine.anything(),
        event,
                `/upload/${id}/dropbox/move?path=data.csv&to=eidchub`,
                'POST',
                jasmine.any(Function)
      )
    })
  })
  describe('confirm buttons', function () {
    beforeEach(function () {
      this.event = new Event('click')
      spyOn(view, 'showInProgress').and.returnValue('btn test')
      spyOn(view, 'request')
      spyOn(view, 'showNormal')
    })
    describe('cancel', function () {
      it('trigger', function () {
        // given
        model.set({
          datastore: 'dropbox',
          moving: true
        })
        view.render()
        const $cancelBtn = view.$('.cancel')
        spyOn(view, 'cancel')
        view.delegateEvents()

        // when
        $cancelBtn.trigger('click')

        // then
        expect($cancelBtn).toHaveSize(1)
        expect(view.cancel).toHaveBeenCalled()
      })

      it('cancel', function () {
        // given
        spyOn(window, 'confirm').and.returnValue(false)

        // when
        view.cancel(this.event)

        // then
        expect(view.request).not.toHaveBeenCalled()
        expect(view.showNormal).toHaveBeenCalled()
      })

      it('ok', function () {
        // given
        spyOn(window, 'confirm').and.returnValue(true)

        // when
        view.cancel(this.event)

        // then
        // noinspection JSCheckFunctionSignatures
        expect(view.request).toHaveBeenCalledWith(
          jasmine.anything(),
          this.event,
                    `/upload/${id}/eidchub/cancel?path=data.csv`,
                    'POST',
                    jasmine.any(Function)
        )
      })
    })

    describe('ignore', function () {
      it('trigger', function () {
        // given
        model.set({
          action: 'ignore',
          datastore: 'dropbox'
        })
        view.render()
        const $ignoreBtn = view.$('.ignore')
        spyOn(view, 'ignore')
        view.delegateEvents()

        // when
        $ignoreBtn.trigger('click')

        // then
        expect($ignoreBtn).toHaveSize(1)
        expect(view.ignore).toHaveBeenCalled()
      })

      it('cancel', function () {
        // given
        spyOn(window, 'confirm').and.returnValue(false)

        // when
        view.ignore(this.event)

        // then
        expect(view.request).not.toHaveBeenCalled()
        expect(view.showNormal).toHaveBeenCalled()
      })

      it('ok', function () {
        // given
        spyOn(window, 'confirm').and.returnValue(true)

        // when
        view.ignore(this.event)

        // then
        // noinspection JSCheckFunctionSignatures
        expect(view.request).toHaveBeenCalledWith(
          jasmine.anything(),
          this.event,
                    `/upload/${id}/eidchub/unregister?path=data.csv`,
                    'POST',
                    jasmine.any(Function)
        )
      })
    })
    describe('delete', function () {
      it('trigger', function () {
        // given
        model.set({ action: 'move-both' })
        view.render()
        const $deleteBtn = view.$('.delete')
        spyOn(view, 'delete')
        view.delegateEvents()

        // when
        $deleteBtn.trigger('click')

        // then
        expect($deleteBtn).toHaveSize(1)
        expect(view.delete).toHaveBeenCalled()
      })

      it('cancel', function () {
        // given
        spyOn(window, 'confirm').and.returnValue(false)

        // when
        view.delete(this.event)

        // then
        expect(view.request).not.toHaveBeenCalled()
        expect(view.showNormal).toHaveBeenCalled()
      })

      it('ok', function () {
        // given
        spyOn(window, 'confirm').and.returnValue(true)

        // when
        view.delete(this.event)

        // then
        // noinspection JSCheckFunctionSignatures
        expect(view.request).toHaveBeenCalledWith(
          jasmine.anything(),
          this.event,
                    `/upload/${id}/eidchub?path=data.csv`,
                    'DELETE',
                    jasmine.any(Function)
        )
      })
    })
  })

  describe('accept', function () {
    it('trigger', function () {
      // given
      model.set({
        action: 'accept',
        datastore: 'dropbox'
      })
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
      const event = new Event('click')
      spyOn(view, 'showInProgress')
      spyOn(view, 'request')

      // when
      view.accept(event)

      // then
      expect(view.showInProgress).toHaveBeenCalledWith(event)
      // noinspection JSCheckFunctionSignatures
      expect(view.request).toHaveBeenCalledWith(
        jasmine.anything(),
        event,
                `/upload/${id}/eidchub/accept?path=data.csv`,
                'POST',
                jasmine.any(Function)
      )
    })
  })

  xdescribe('request', function () {
    const event = new Event('click')
    const url = `/upload/${id}/eidchub/accept?path=data.csv`
    const method = 'POST'
    const success = jasmine.createSpy('success')

    it('without response body', function () {
      // given

      // when
      view.request(event, url, method, success)

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.success)

      expect(request.method).toBe(method)
      expect(request.url).toBe(url)
      expect(success).toHaveBeenCalled()
    })

    it('with response body', function () {
      // given

      // when
      view.request(event, url, method, success)

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.serverState)

      expect(request.method).toBe(method)
      expect(request.url).toBe(url)
      // noinspection JSCheckFunctionSignatures
      expect(success).toHaveBeenCalledWith(response, 'success', jasmine.anything())
    })

    it('error', function () {
      // given
      spyOn(view, 'showInError')

      // when
      view.request(event, url, method, success)

      // then
      const request = jasmine.Ajax.requests.mostRecent()
      // noinspection JSCheckFunctionSignatures
      request.respondWith(testResponses.error)

      expect(request.method).toBe(method)
      expect(request.url).toBe(url)
      expect(success).not.toHaveBeenCalled()
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
