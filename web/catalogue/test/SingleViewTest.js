import { EditorMetadata, SingleView } from '../src/Editor'

describe('Test SingleView', () => {
  let view = null

  beforeEach(() => {
    const model = new EditorMetadata({ title: 'some text' })
    view = new SingleView({
      model
    })
  })

  it('View should be defined', () => {
    // then
    expect(view).toBeDefined()
  })

  it('show should make el visible', () => {
    // when
    view.render() // show is called inside of render
    // then
    expect(view.$el.hasClass('visible')).toBeTrue()
  })

  it('hide should make el invisible', () => {
    // when
    view.render()
    view.hide()
    // then
    expect(view.$el.hasClass('visible')).toBeFalse()
  })
})
