import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import $ from 'jquery'
import Backbone from 'backbone'
import { FileView } from '../src/File'

describe('Test FileView', function () {
  let el = null
  let model = null
  let view = null

  beforeEach(function () {
    el = $('ul').appendTo($('body'))
    vi.spyOn(FileView.prototype, 'remove')
    vi.spyOn(FileView.prototype, 'render')
    model = new Backbone.Model({
      message: 'Hello World',
      type: 'info'
    })
    view = new FileView({
      el,
      model
    })
  })

  afterEach(() => el.remove())

  it('has DOM events', function () {
    expect(view.events['change input']).toBeDefined()
    expect(view.events['change input']).toEqual('select')
  })

  it('rerenders when model changed', function () {
    // when
    model.set('toDelete', true)

    // then
    expect(view.render).toHaveBeenCalled()
  })
})
