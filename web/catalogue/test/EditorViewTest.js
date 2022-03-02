import { EditorView } from '../src/Editor'
import Backbone from 'backbone'

describe('Test EditorView', () => {
  let view = null
  let model = null

  beforeEach(() => {
    const options = ({ value: 'value', label: 'title' }, { value: 'value2', label: 'title2' },
    { value: 'value3', label: 'title3' })

    model = new Backbone.Model({
      options: options
    })

    const ViewType = EditorView.extend({
      initialize () {
        this.sections = [{
          label: 'One',
          title: 'Catalogue',
          views: [
            new Backbone.View({
              model: this.model,
              modelAttribute: 'value',
              label: 'Catalogue',
              options: this.model.options,
              helpText: '<p>Catalogue</p>'
            })
          ]
        }
        ]
        return EditorView.prototype.initialize.apply(this)
      }
    })
    view = new ViewType({ model: model })
  })

  it('View should be defined', () => {
    // then
    expect(view).toBeDefined()
  })

  it('attemptDelete should be called', () => {
    spyOn(view, 'attemptDelete')
    view.initialize()
    view.delegateEvents()
    // when
    view.$('#editorDelete').trigger('click')

    // then
    expect(view.attemptDelete).toHaveBeenCalled()
  })

  it('delete should be called', () => {
    // given
    spyOn(view, 'delete')
    view.initialize()
    view.delegateEvents()

    // when
    view.$('#confirmDeleteYes').trigger('click')

    // then
    expect(view.delete).toHaveBeenCalled()
  })

  it('attemptExit should be called', () => {
    // given
    spyOn(view, 'attemptExit')
    view.initialize()
    view.delegateEvents()

    // when
    view.$('#editorExit').trigger('click')

    // then
    expect(view.attemptExit).toHaveBeenCalled()
  })

  it('exit should be called', () => {
    // given
    spyOn(view, 'exit')
    view.initialize()
    view.delegateEvents()

    // when
    view.$('#exitWithoutSaving').trigger('click')

    // then
    expect(view.exit).toHaveBeenCalled()
  })

  it('save should be called', () => {
    // given
    spyOn(view, 'save')
    view.initialize()
    view.delegateEvents()

    // when
    view.$('#editorSave').trigger('click')

    // then
    expect(view.save).toHaveBeenCalled()
  })

  it('next should be called', () => {
    // given
    spyOn(view, 'next')
    view.initialize()
    view.delegateEvents()

    // when
    view.$('#editorNext').trigger('click')

    // then
    expect(view.next).toHaveBeenCalled()
  })

  it('direct should be called', () => {
    // given
    spyOn(view, 'direct')
    view.initialize()
    view.delegateEvents()

    // when
    view.$('#editorNav li').trigger('click')

    // then
    expect(view.direct).toHaveBeenCalled()
  })
})
