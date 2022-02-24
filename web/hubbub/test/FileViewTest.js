import $ from 'jquery'
import Backbone from 'backbone'
import { FileView, FileCollection } from '../src/File'

describe('Test FileView', function () {
  const el = null
  let model = null
  let view = null

  beforeEach(function () {
    model = new Backbone.Model({
      url: 'test.com',
      datastore: 'datastore',
      metadata: 'metadata',
      classes: 'classes',
      errorType: 'errorType',
      path: 'path',
      size: 'size',
      hash: 'hash',
      date: 'date',
      estimate: 'estimate',
      message: 'message',
      moving: 'moving',
      status: 'VALID'

    })
    view = new FileView({
      collection: new FileCollection(),
      datastore: new FileCollection(),
      metadata: new FileCollection(),
      model,
      url: '/file/post'
    })
    const container = $('<div id="document-upload"></div>')
    $(document.body).append(container)
  })

  it('is defined', function () {
    // then
    expect(view).toBeDefined()
  })

  it('validate should be triggered', () => {
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

  it('moveDatastore should be triggered', () => {
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

  it('moveDatastore should be triggered', () => {
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

  xit('showDelete should be triggered', () => {
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

  xit('showIgnore should be triggered', () => {
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

  xit('showCancel should be triggered', () => {
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

  xit('accept should be triggered', () => {
    // given
    view.render()
    view.delegateEvents()
    spyOn(view, 'accept')
    model.set({ action: 'accept' })

    // when
    view.$('.accept').trigger('click')

    // then

    expect(view.accept).toHaveBeenCalled()
  })

  xit('expand should be triggered', () => {
    // given
    view.render()
    view.delegateEvents()
    spyOn(view, 'expand')

    // when
    view.$('.panel-heading').trigger('click')

    // then

    expect(view.expand).toHaveBeenCalled()
  })
})
