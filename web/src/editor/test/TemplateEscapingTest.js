import { afterEach, describe, expect, it } from 'vitest'
import $ from 'jquery'
import Backbone from 'backbone'
import { InputView } from '../src'
import { TextareaView, ContactView } from '../src/views'
import { Contact } from '../src/models'
import { MessageView } from '../../search/src/views'

/**
 * Record data, server responses and URL fragments must reach the page as text. Unescaped
 * interpolation ran markup stored in a record in the next editor's browser, and cut a value short at
 * its first double quote (NERC-CEH/dri-one#454). scripts/check-template-escaping.js guards every
 * template; these specs pin the behaviour of the main sinks.
 */
describe('Template escaping', () => {
  const payload = 'The "X" dataset <b>bold</b> & <img src=x onerror="window.__xss454=1">'

  afterEach(() => { delete window.__xss454 })

  it('keeps a title exactly, as text, in an input', () => {
    const view = new InputView({ model: new Backbone.Model({ title: payload }), modelAttribute: 'title' })

    expect(view.$('input').val()).toBe(payload)
    expect(view.$('b, img').length).toBe(0)
  })

  it('keeps a description exactly, as text, in a textarea', () => {
    const description = `</textarea>${payload}`
    const view = new TextareaView({ model: new Backbone.Model({ description }), modelAttribute: 'description' })

    expect(view.$('textarea').val()).toBe(description)
    expect(view.$('b, img').length).toBe(0)
  })

  it('keeps a contact organisation exactly, as text, in a row', () => {
    const model = new Contact({ organisationName: payload })
    const view = new ContactView({ model, index: 'c1' })

    expect(view.$('[data-name="organisationName"]').val()).toBe(payload)
    expect(view.$('b, img').length).toBe(0)
  })

  it('shows an error message as text', () => {
    // The permission page built this message from the URL fragment
    const panel = $('<div id="message-panel">').appendTo(document.body)
    const app = new Backbone.Model()
    const messages = new MessageView({ model: app })

    app.trigger('error', `Unable to load permission for: ${payload}`)

    expect(panel.find('.alert').text()).toContain(payload)
    expect(panel.find('b, img').length).toBe(0)
    messages.remove()
    panel.remove()
  })
})
