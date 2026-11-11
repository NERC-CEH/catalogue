import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import $ from 'jquery'
import Backbone from 'backbone'
import { EditorView } from '../src'
import { fakeServer, settle } from '../../test/support'
import { dialog } from '../../dialog/src/dialog'

// What the editor does around its sections: moving between them, saving, deleting, leaving with or
// without unsaved changes, and reporting what the server or validation says (dri-one #473).
// Backbone.history.location is replaced, so leaving or reloading the page is recorded, not done.
describe('EditorView behaviour', () => {
  let view, model, location, sections

  const section = label => ({ label, title: label, views: [{ el: document.createElement('div'), show: vi.fn(), hide: vi.fn() }] })

  function editor (attributes = { id: 'abc', title: 'Rainfall' }) {
    model = new Backbone.Model(attributes)
    model.urlRoot = '/documents'
    sections = [section('One'), section('Two'), section('Three')]
    const Editor = EditorView.extend({
      initialize () {
        this.sections = sections
        return EditorView.prototype.initialize.apply(this)
      }
    })
    view = new Editor({ model, el: $('<div>').appendTo(document.body) })
    return view
  }

  const answer = isConfirmed => dialog.confirm.mockResolvedValue({ isConfirmed })

  beforeEach(() => {
    fakeServer.install()
    vi.spyOn(dialog, 'confirm').mockResolvedValue({ isConfirmed: false })
    vi.spyOn(dialog, 'alert').mockResolvedValue({ isConfirmed: true })
    $('html').attr('data-catalogue', 'eidc').removeData('catalogue')
    location = { pathname: '/eidc/documents', replace: vi.fn(), reload: vi.fn() }
    Object.defineProperty(Backbone.history, 'location', { value: location, configurable: true, writable: true })
  })

  afterEach(() => {
    fakeServer.uninstall()
    delete Backbone.history.location
    Backbone.history.location = window.location
    $('html').removeAttr('data-catalogue')
    $('body').removeClass('edit-mode')
    $(document.body).empty()
  })

  describe('moving between sections', () => {
    it('opens on the first, with Back disabled', () => {
      editor()

      expect(view.currentStep).toBe(1)
      expect(sections[0].views[0].show).toHaveBeenCalled()
      expect(view.$('.editor-nav span').map((i, span) => span.textContent).get()).toEqual(['One', 'Two', 'Three'])
      expect(view.$('.editor-nav span').first().hasClass('active')).toBe(true)
    })

    it('shows the next section and hides the rest, up to the last', () => {
      editor()

      view.next()
      expect(view.currentStep).toBe(2)
      expect(sections[1].views[0].show).toHaveBeenCalled()
      expect(sections[0].views[0].hide).toHaveBeenCalled()
      expect(view.$('#editorBack').prop('disabled')).toBe(false)
      expect(view.$('.editor-nav span').eq(1).hasClass('active')).toBe(true)

      view.next()
      view.next()
      expect(view.currentStep).toBe(3)
      expect(view.$('#editorNext').prop('disabled')).toBe(true)
    })

    it('goes back, but not before the first', () => {
      editor()
      view.navigate(2)

      view.back()
      view.back()

      expect(view.currentStep).toBe(1)
      expect(view.$('#editorBack').prop('disabled')).toBe(true)
    })

    it('goes straight to a section chosen from the menu', () => {
      editor()

      view.$('.editor-nav span').eq(2).trigger('click')

      expect(view.currentStep).toBe(3)
      expect(sections[2].views[0].show).toHaveBeenCalled()
    })
  })

  describe('changes', () => {
    it('notices a change, and forgets it once saved', () => {
      editor()
      expect(view.saveRequired).toBe(false)

      model.set('title', 'Rain')
      expect(view.saveRequired).toBe(true)

      model.trigger('sync')
      expect(view.saveRequired).toBe(false)
    })

    it('lists what validation refused', () => {
      editor()

      model.trigger('invalid', model, ['A title is mandatory', 'Embargo date is in the past or too far in the future'])

      expect(dialog.alert).toHaveBeenCalledWith(expect.objectContaining({
        title: 'Validation Errors',
        text: ['A title is mandatory', 'Embargo date is in the past or too far in the future']
      }))
    })
  })

  describe('deleting', () => {
    it('deletes once confirmed, then goes back to the catalogue\'s search', async () => {
      editor()
      answer(true)

      view.$('#editorDelete').trigger('click')
      await settle()
      const request = fakeServer.lastRequest()
      expect(request.method).toBe('DELETE')
      expect(request.url).toBe('/documents/abc')
      request.respondWith({ status: 204, contentType: 'application/json', responseText: '' })

      expect(document.body.contains(view.el)).toBe(false)
      expect(location.replace).toHaveBeenCalledWith('/eidc/documents')
      expect(dialog.alert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Deleted!' }))
    })

    it('keeps the record when the editor cancels', async () => {
      editor()

      view.$('#editorDelete').trigger('click')
      await settle()

      expect(fakeServer.requestCount()).toBe(0)
      expect(location.replace).not.toHaveBeenCalled()
    })
  })

  describe('leaving', () => {
    it('leaves straight away with nothing unsaved, back to the record it was opened from search', () => {
      editor()

      view.$('#editorExit').trigger('click')

      expect(dialog.confirm).not.toHaveBeenCalled()
      expect(document.body.contains(view.el)).toBe(false)
      expect($('body').hasClass('edit-mode')).toBe(false)
      expect(location.replace).toHaveBeenCalledWith('/documents/abc')
    })

    it('reloads the page when leaving a new record, or one opened on its own page', () => {
      editor({ title: 'New' })
      view.exit()
      expect(location.reload).toHaveBeenCalledTimes(1)

      location.pathname = '/documents/abc'
      editor()
      view.exit()
      expect(location.reload).toHaveBeenCalledTimes(2)
    })

    it('asks first when there are unsaved changes, and stays if told to', async () => {
      editor()
      model.set('title', 'Rain')

      view.$('#editorExit').trigger('click')
      await settle()

      expect(dialog.confirm).toHaveBeenCalledWith(expect.objectContaining({ title: 'There are unsaved changes.' }))
      expect(location.replace).not.toHaveBeenCalled()
    })

    it('leaves without saving when told to', async () => {
      editor()
      model.set('title', 'Rain')
      answer(true)

      view.$('#editorExit').trigger('click')
      await settle()

      expect(location.replace).toHaveBeenCalledWith('/documents/abc')
    })
  })

  describe('an edit conflict', () => {
    it('offers to copy the unsaved edits to the clipboard', async () => {
      editor()
      model.set('title', 'Rain')
      const writeText = vi.spyOn(navigator.clipboard, 'writeText').mockResolvedValue()

      model.trigger('error', model, { status: 409 })
      const options = dialog.alert.mock.lastCall[0]
      const [copy] = options.body
      copy.click()
      await settle()

      expect(options.title).toBe('Edit conflict')
      expect(writeText).toHaveBeenCalledWith(JSON.stringify(model.toJSON(), null, 2))
      expect(copy.textContent).toBe('Copied!')
    })

    it('falls back to copying the selected text where the clipboard API is missing', async () => {
      editor()
      const clipboard = Object.getOwnPropertyDescriptor(Navigator.prototype, 'clipboard')
      Object.defineProperty(navigator, 'clipboard', { value: undefined, configurable: true })
      vi.spyOn(document, 'execCommand').mockReturnValue(true)

      try {
        model.trigger('error', model, { status: 409 })
        const [copy, payload] = dialog.alert.mock.lastCall[0].body
        // Selecting text needs it on the page, as it is while the dialog is open
        $(document.body).append(copy, payload)
        copy.click()
        await settle()

        expect(document.execCommand).toHaveBeenCalledWith('copy')
        expect(copy.textContent).toBe('Copied!')
      } finally {
        delete navigator.clipboard
        if (clipboard) Object.defineProperty(Navigator.prototype, 'clipboard', clipboard)
      }
    })
  })

  it('reports another server error with its message', () => {
    editor()

    model.trigger('error', model, { status: 500, statusText: 'Server Error', responseJSON: { message: 'Solr is down' } })

    expect(dialog.alert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Server response: 500 Server Error', icon: 'error', text: 'Solr is down' }))
  })
})
