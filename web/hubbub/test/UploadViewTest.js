import $ from 'jquery'
import { HubbubUploadModel, HubbubUploadView } from '../src/Upload'

describe('Test DropzoneView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    const id = $('#document-upload').data('guid')
    model = new HubbubUploadModel()
    model.set('id', id)
    view = new HubbubUploadView({
      model
    })
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

  it('moveAllDatastore should be triggered', () => {
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

  it('reschedule should be triggered', () => {
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

  it('schedule should be triggered', () => {
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

  it('validate-all should be triggered', () => {
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
