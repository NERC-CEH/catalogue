import { EditorView } from '../src'
import Backbone from 'backbone'
import Swal from 'sweetalert2'

describe('EditorView', () => {
  let view = null
  let model = null

  beforeEach(() => {
    const options = (
      { value: 'value', label: 'title' },
      { value: 'value2', label: 'title2' },
      { value: 'value3', label: 'title3' }
    )

    model = new Backbone.Model({
      options
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
    view = new ViewType({ model })
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
    view.$('.editor-nav span').trigger('click')

    // then
    expect(view.direct).toHaveBeenCalled()
  })

  it('save shows a "Saving…" state and defers "Saved!" until the request succeeds', () => {
    // given: a save that is in flight (truthy jqXHR), not yet resolved
    spyOn(view.model, 'save').and.returnValue({})
    spyOn(Swal, 'fire')
    view.saveRequired = true

    // when
    view.save()

    // then: button is disabled + "Saving…" and the popup is NOT shown yet
    expect(view.model.save).toHaveBeenCalled()
    expect(view.$('#editorSave').prop('disabled')).toBe(true)
    expect(view.$('#editorSave').text()).toContain('Saving')
    expect(Swal.fire).not.toHaveBeenCalled()
    expect(view.saveRequired).toBe(true)

    // and when the server confirms, the success callback shows the popup and
    // restores the button
    const options = view.model.save.calls.mostRecent().args[1]
    expect(options.wait).toBe(true)
    options.success()

    expect(Swal.fire).toHaveBeenCalledWith({ title: 'Saved!', icon: 'success' })
    expect(view.$('#editorSave').prop('disabled')).toBe(false)
    expect(view.$('#editorSave').text()).toContain('Save')
    expect(view.saveRequired).toBe(false)
  })

  it('re-enables the save button when validation blocks the request or the save fails', () => {
    // validation failure: save() returns false, no request sent
    spyOn(view.model, 'save').and.returnValue(false)
    view.save()
    expect(view.$('#editorSave').prop('disabled')).toBe(false)

    // server error: the error callback restores the button
    view.model.save.and.returnValue({})
    view.save()
    expect(view.$('#editorSave').prop('disabled')).toBe(true)
    view.model.save.calls.mostRecent().args[1].error()
    expect(view.$('#editorSave').prop('disabled')).toBe(false)
  })

  it('shows the ajax spinner only while a request is in flight', () => {
    // #editorAjax is hidden until a request starts, then hidden again on completion
    expect(view.$('#editorAjax').hasClass('visible')).toBe(false)

    view.model.trigger('request')
    expect(view.$('#editorAjax').hasClass('visible')).toBe(true)

    view.model.trigger('sync')
    expect(view.$('#editorAjax').hasClass('visible')).toBe(false)
  })

  it('shows a conflict banner and preserves edits on 409', () => {
    const swalSpy = spyOn(Swal, 'fire')
    view.model.set('title', 'my in-progress title')

    // Simulate Backbone's error event for a 409 conflict
    view.model.trigger('error', view.model, { status: 409, statusText: 'Conflict', responseJSON: {} })

    expect(swalSpy).toHaveBeenCalled()
    const args = swalSpy.calls.mostRecent().args[0]
    expect(args.title).toBe('Edit conflict')
    // this must be the conflict-specific banner, not the generic error dialog
    // (whose title also happens to contain "Conflict" via statusText)
    expect(args.title).not.toContain('Server response')
    expect(args.icon).toBe('warning')
    // edits are still on the model
    expect(view.model.get('title')).toBe('my in-progress title')
  })
})
