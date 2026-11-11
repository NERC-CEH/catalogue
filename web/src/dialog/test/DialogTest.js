import { afterEach, describe, expect, it, vi } from 'vitest'
import $ from 'jquery'
import { dialog } from '../src/dialog'

// The dialogs on Bootstrap's own modal, opened and closed for real, as a user would
describe('dialog', () => {
  const open = () => vi.waitFor(() => expect(document.querySelector('.modal.show')).not.toBeNull())
  const shown = () => document.querySelector('.modal.show')
  const buttons = () => [...shown().querySelectorAll('.modal-footer button')].map(each => each.textContent)

  // Closed as a user would rather than removed: a dialog that never closes never resolves, and every
  // dialog after it would wait for it
  afterEach(async () => {
    for (let modal; (modal = document.querySelector('.modal'));) {
      await open()
      modal.querySelector('.btn-close').click()
      await vi.waitFor(() => expect(modal.isConnected).toBe(false))
      await new Promise(resolve => setTimeout(resolve, 0))
    }
    expect($('.modal-backdrop').length).toBe(0)
  })

  describe('confirm', () => {
    it('resolves isConfirmed once the confirm button is pressed, and is gone afterwards', async () => {
      const answer = dialog.confirm({ title: 'Are you sure?', text: 'This deletes the record', confirmText: 'Yes, delete it', danger: true })
      await open()

      expect(shown().querySelector('.modal-title').textContent).toBe('Are you sure?')
      expect(shown().querySelector('.modal-body').textContent).toBe('This deletes the record')
      expect(buttons()).toEqual(['Cancel', 'Yes, delete it'])
      expect(shown().querySelector('[data-dialog-confirm]').classList).toContain('btn-danger')
      // Not the button that deletes, for something that can't be undone
      await vi.waitFor(() => expect(document.activeElement?.textContent).toBe('Cancel'))

      shown().querySelector('[data-dialog-confirm]').click()

      expect(await answer).toEqual({ isConfirmed: true })
      expect(document.querySelector('.modal')).toBeNull()
    })

    it('resolves not confirmed when cancelled', async () => {
      const answer = dialog.confirm({ title: 'Are you sure?' })
      await open()

      $(shown()).find('.modal-footer [data-bs-dismiss]').trigger('click')

      expect(await answer).toEqual({ isConfirmed: false })
    })

    it('resolves not confirmed when closed with Escape', async () => {
      const answer = dialog.confirm({ title: 'Are you sure?' })
      await open()

      shown().dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }))

      expect(await answer).toEqual({ isConfirmed: false })
    })

    it('warns, with a primary button that has the focus, unless told otherwise', async () => {
      dialog.confirm({ title: 'Are you sure?' })
      await open()
      await vi.waitFor(() => expect(document.activeElement).toBe(shown().querySelector('[data-dialog-confirm]')))

      expect(shown().querySelector('.modal-title i').className).toContain('fa-triangle-exclamation')
      expect(shown().querySelector('[data-dialog-confirm]').classList).toContain('btn-primary')
      expect(buttons()).toEqual(['Cancel', 'OK'])
    })
  })

  describe('alert', () => {
    it('has a single Close button, and resolves once closed', async () => {
      const closed = dialog.alert({ title: 'Saved!', icon: 'success' })
      await open()

      expect(buttons()).toEqual(['Close'])
      expect(shown().querySelector('.modal-title i').className).toContain('fa-circle-check')

      shown().querySelector('[data-dialog-confirm]').click()

      expect(await closed).toEqual({ isConfirmed: true })
    })

    it('puts each line of text in a paragraph of its own, after which comes the body it is given', async () => {
      const textarea = document.createElement('textarea')
      dialog.alert({ title: 'Validation Errors', icon: 'error', text: ['A title is mandatory', 'A box is incomplete'], body: textarea })
      await open()

      const body = shown().querySelector('.modal-body')
      expect([...body.querySelectorAll('p')].map(each => each.textContent)).toEqual(['A title is mandatory', 'A box is incomplete'])
      expect(body.lastElementChild).toBe(textarea)
    })

    it('has no icon it does not know', async () => {
      dialog.alert({ title: 'Notice', icon: 'nonsense' })
      await open()

      expect(shown().querySelector('.modal-title i')).toBeNull()
    })
  })

  // What a dialog shows is text: markup in it is shown, not run (dri-one #531)
  it('shows markup in its title and text as text', async () => {
    window.__dialog531 = undefined
    dialog.alert({ title: '<img src=x onerror="window.__dialog531=1">', text: ['<b>bold</b>', '<img src=x onerror="window.__dialog531=1">'] })
    await open()

    expect(shown().querySelector('img, b')).toBeNull()
    expect(shown().querySelector('.modal-title').textContent).toBe('<img src=x onerror="window.__dialog531=1">')
    expect(shown().querySelector('.modal-body p').textContent).toBe('<b>bold</b>')
    expect(window.__dialog531).toBeUndefined()
  })

  // Bootstrap shows one modal at a time and ignores a show() while another is opening
  it('shows a dialog asked for while another is open once that one has closed', async () => {
    const first = dialog.alert({ title: 'First' })
    const second = dialog.confirm({ title: 'Second' })
    await open()

    expect(document.querySelectorAll('.modal').length).toBe(1)
    expect(shown().querySelector('.modal-title').textContent).toBe('First')

    shown().querySelector('[data-dialog-confirm]').click()
    await first
    await open()

    expect(shown().querySelector('.modal-title').textContent).toBe('Second')
    shown().querySelector('[data-dialog-confirm]').click()
    expect(await second).toEqual({ isConfirmed: true })
  })

  it('gives focus back to what had it once closed', async () => {
    const opener = $('<button type="button" class="opener">Delete</button>').appendTo(document.body)[0]
    opener.focus()

    const answer = dialog.confirm({ title: 'Are you sure?' })
    await open()
    await vi.waitFor(() => expect(document.activeElement).not.toBe(opener))
    shown().querySelector('.btn-close').click()
    await answer

    expect(document.activeElement).toBe(opener)
    opener.remove()
  })

  it('labels the dialog with its title, for a screen reader', async () => {
    dialog.alert({ title: 'Saved!' })
    await open()

    const title = shown().querySelector('.modal-title')
    expect(shown().getAttribute('aria-labelledby')).toBe(title.id)
    expect(shown().getAttribute('role')).toBe('dialog')
    expect(shown().getAttribute('aria-modal')).toBe('true')
  })
})
