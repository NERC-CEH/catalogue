import $ from 'jquery'
import { File, FileCollection, FileView } from '../src/File'

describe('FileView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    model = new File({
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
      url: '/upload/28936d69-34b4-47df-96b4-dd7b45f7dbbd'
    })
  })

  it('initialize', function () {
    // then
    expect(view).toBeDefined()
    expect(view.url).toBe('/upload/28936d69-34b4-47df-96b4-dd7b45f7dbbd')
  })

  xit('validate should be triggered', () => {
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

  xit('moveDatastore should be triggered', () => {
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

  xit('moveDatastore should be triggered', () => {
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
