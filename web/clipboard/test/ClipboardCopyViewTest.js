import $ from 'jquery'
import { ClipboardCopyView } from '../src/ClipboardCopy'

describe('Test ClipboardCopyView', () => {
  let view

  beforeEach(function () {
    view = new ClipboardCopyView()
  })

  it('View should be defined', () => {
    // then
    expect(view).toBeDefined()
  })

  it('copyToClipboard should be called', () => {
    // given
    spyOn(view, 'copyToClipboard')
    view.initialize()
    view.delegateEvents()
    view.render()

    // when
    view.$('button').trigger('click')

    // then
    expect(view.copyToClipboard).toHaveBeenCalled()
  })
})
