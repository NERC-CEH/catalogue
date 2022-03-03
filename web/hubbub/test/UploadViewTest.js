import $ from 'jquery'
import { HubbubUploadModel, HubbubUploadView } from '../src/Upload'

describe('Test UploadView', function () {
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
    const container = $('<div class ="dropzone-container"><div class="dropzone-files"><div class="fileinput-button"/></div></div>')
    $(document.body).append(container)
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

  xit('loadDropbox should be triggered', () => {
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
    const container = $('<div class ="dropzone-container"><div class="dropzone-files"><div class="fileinput-button"/></div></div>')
    $(document.body).append(container)
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
