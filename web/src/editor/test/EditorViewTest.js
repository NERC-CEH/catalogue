import { beforeEach, describe, expect, it, vi } from 'vitest'
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
    vi.spyOn(view, 'attemptDelete').mockImplementation(() => {})
    view.initialize()
    view.delegateEvents()
    // when
    view.$('#editorDelete').trigger('click')

    // then
    expect(view.attemptDelete).toHaveBeenCalled()
  })

  it('attemptExit should be called', () => {
    // given
    vi.spyOn(view, 'attemptExit').mockImplementation(() => {})
    view.initialize()
    view.delegateEvents()

    // when
    view.$('#editorExit').trigger('click')

    // then
    expect(view.attemptExit).toHaveBeenCalled()
  })

  it('save should be called', () => {
    // given
    vi.spyOn(view, 'save').mockImplementation(() => {})
    view.initialize()
    view.delegateEvents()

    // when
    view.$('#editorSave').trigger('click')

    // then
    expect(view.save).toHaveBeenCalled()
  })

  it('next should be called', () => {
    // given
    vi.spyOn(view, 'next').mockImplementation(() => {})
    view.initialize()
    view.delegateEvents()

    // when
    view.$('#editorNext').trigger('click')

    // then
    expect(view.next).toHaveBeenCalled()
  })

  it('direct should be called', () => {
    // given
    vi.spyOn(view, 'direct').mockImplementation(() => {})
    view.initialize()
    view.delegateEvents()

    // when
    view.$('.editor-nav span').trigger('click')

    // then
    expect(view.direct).toHaveBeenCalled()
  })

  it('save shows a "Saving…" state and defers "Saved!" until the request succeeds', () => {
    // given: a save that is in flight (truthy jqXHR), not yet resolved
    vi.spyOn(view.model, 'save').mockReturnValue({})
    vi.spyOn(Swal, 'fire').mockImplementation(() => {})
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
    const options = view.model.save.mock.lastCall[1]
    expect(options.wait).toBe(true)
    options.success()

    expect(Swal.fire).toHaveBeenCalledWith({ title: 'Saved!', icon: 'success' })
    expect(view.$('#editorSave').prop('disabled')).toBe(false)
    expect(view.$('#editorSave').text()).toContain('Save')
    expect(view.saveRequired).toBe(false)
  })

  it('re-enables the save button when validation blocks the request or the save fails', () => {
    // validation failure: save() returns false, no request sent
    vi.spyOn(view.model, 'save').mockReturnValue(false)
    view.save()
    expect(view.$('#editorSave').prop('disabled')).toBe(false)

    // server error: the error callback restores the button
    view.model.save.mockReturnValue({})
    view.save()
    expect(view.$('#editorSave').prop('disabled')).toBe(true)
    view.model.save.mock.lastCall[1].error()
    expect(view.$('#editorSave').prop('disabled')).toBe(false)
  })

  // A save showed a second spinner beside the buttons, on every request the record made, as well as
  // the one in the Save button. The button that started the request is the only one now.
  const spinners = () => view.$('.editor-buttons .fa-spinner')

  it('shows a save in progress in the Save button only', () => {
    vi.spyOn(view.model, 'save').mockReturnValue({})

    view.save()
    view.model.trigger('request')

    expect(spinners().length).toBe(1)
    expect(spinners().closest('#editorSave').length).toBe(1)
  })

  it('shows a delete in progress in the Delete button, and gives the button back if it fails', () => {
    vi.spyOn(view.model, 'destroy').mockReturnValue({})

    view.delete()
    view.model.trigger('request')

    expect(spinners().length).toBe(1)
    expect(view.$('#editorDelete').prop('disabled')).toBe(true)
    expect(view.$('#editorDelete').text()).toContain('Deleting')

    view.model.destroy.mock.lastCall[0].error()

    expect(spinners().length).toBe(0)
    expect(view.$('#editorDelete').prop('disabled')).toBe(false)
    expect(view.$('#editorDelete').text()).toContain('Delete')
  })

  it('shows a conflict banner and preserves edits on 409', () => {
    const swalSpy = vi.spyOn(Swal, 'fire').mockImplementation(() => {})
    view.model.set('title', 'my in-progress title')

    // Simulate Backbone's error event for a 409 conflict
    view.model.trigger('error', view.model, { status: 409, statusText: 'Conflict', responseJSON: {} })

    expect(swalSpy).toHaveBeenCalled()
    const args = swalSpy.mock.lastCall[0]
    expect(args.title).toBe('Edit conflict')
    // this must be the conflict-specific banner, not the generic error dialog
    // (whose title also happens to contain "Conflict" via statusText)
    expect(args.title).not.toContain('Server response')
    expect(args.icon).toBe('warning')
    // the dialog must actually render the submitted edits, not just promise them
    expect(args.html).toContain('my in-progress title')
    // edits are still on the model
    expect(view.model.get('title')).toBe('my in-progress title')
  })

  it('shows the record JSON in the save-error dialog as text (dri-one #454)', () => {
    const swalSpy = vi.spyOn(Swal, 'fire').mockImplementation(() => {})
    view.model.set('title', '</textarea><img src=x onerror="window.__xss454=1">')

    view.model.trigger('error', view.model, { status: 500, statusText: 'Server Error', responseText: 'boom' })

    const html = swalSpy.mock.lastCall[0].html
    expect(html).not.toContain('<img')
    expect(html).toContain('&lt;/textarea&gt;&lt;img')
  })
})
