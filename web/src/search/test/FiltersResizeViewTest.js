import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import $ from 'jquery'
import FiltersResizeView from '../src/views/FiltersResizeView'

// The handle between the filters (or the spatial search map) and the results, dragged to give the
// filters more or less of the page
describe('Resizing the filters', () => {
  const KEY = 'search.filterWidth'
  let view

  function page () {
    $(document.body).html(`
      <div class="search" style="display:flex;width:1200px;--filter-width:350px">
        <div class="filters" style="flex:0 0 var(--filter-width)"></div>
        <div class="filters-resize" style="flex:0 0 8px"></div>
        <div style="flex:1"></div>
      </div>`)
    view = new FiltersResizeView({ el: $('.filters-resize') })
    return view
  }
  const width = () => Math.round($('.filters')[0].getBoundingClientRect().width)
  const pointer = (type, clientX) => view.el.dispatchEvent(new PointerEvent(type, { clientX, pointerId: 1, bubbles: true }))
  const drag = (from, to) => { pointer('pointerdown', from); pointer('pointermove', to); pointer('pointerup', to) }
  const key = name => view.$el.trigger($.Event('keydown', { key: name }))

  beforeEach(() => { try { localStorage.removeItem(KEY) } catch (e) {} })
  afterEach(() => { try { localStorage.removeItem(KEY) } catch (e) {} $(document.body).empty() })

  it('follows the handle as it is dragged', () => {
    page()

    drag(350, 450)

    expect(width()).toBe(450)
  })

  it('keeps the filters between a usable width and most of the page', () => {
    page()

    drag(350, 0)
    expect(width()).toBe(250)

    drag(250, 1200)
    expect(width()).toBe(720)
  })

  it('remembers the width for the next search', () => {
    page()
    drag(350, 500)

    page()

    expect(width()).toBe(500)
  })

  it('ignores a remembered width it can\'t use', () => {
    try { localStorage.setItem(KEY, 'wide') } catch (e) {}

    page()

    expect(width()).toBe(350)
  })

  it('leaves the stylesheet\'s width when nothing is remembered, even while the filters are hidden', () => {
    // given a window too narrow to show the filters, when the page starts
    $(document.body).html(`
      <div class="search" style="display:flex;width:1200px">
        <div class="filters" style="flex:0 0 var(--filter-width, 350px);display:none"></div>
        <div class="filters-resize"></div>
      </div>`)
    view = new FiltersResizeView({ el: $('.filters-resize') })

    // when the window is widened
    $('.filters').css('display', 'block')

    // then
    expect(width()).toBe(350)
  })

  it('can be moved from the keyboard, and says how wide the filters are', () => {
    page()

    key('ArrowRight')
    key('ArrowRight')
    key('ArrowLeft')

    expect(width()).toBe(370)
    expect(view.$el.attr('aria-valuenow')).toBe('370')
  })

  it('does nothing when the pointer moves without being pressed on it', () => {
    page()

    pointer('pointermove', 600)

    expect(width()).toBe(350)
  })
})
