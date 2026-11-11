import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { sortable } from '../src/utils/sortable'

// The editor's reorderable rows, which replaced jQuery UI's sortable (dri-one #533)
describe('A reorderable list', () => {
  let list, moved

  const rows = () => [...list.children].map(row => row.dataset.name)
  const middle = element => {
    const box = element.getBoundingClientRect()
    return { clientX: box.left + 5, clientY: box.top + box.height / 2 }
  }
  const pointer = (target, type, at, extra = {}) => {
    const event = new PointerEvent(type, { bubbles: true, cancelable: true, button: 0, pointerType: 'mouse', ...at, ...extra })
    target.dispatchEvent(event)
    return event
  }
  // Presses on from, moves the pointer to the y given, and lets go
  const drag = (from, clientY, extra) => {
    const start = middle(from)
    pointer(from, 'pointerdown', start, extra)
    pointer(document, 'pointermove', { clientX: start.clientX, clientY })
    pointer(document, 'pointerup', { clientX: start.clientX, clientY })
  }
  const row = name => list.querySelector(`[data-name=${name}]`)
  const below = name => row(name).getBoundingClientRect().bottom - 2
  const above = name => row(name).getBoundingClientRect().top + 2

  beforeEach(() => {
    document.body.innerHTML = `<div class="list">
      <div data-name="a" style="height: 40px">A <input type="text"></div>
      <div data-name="b" style="height: 40px">B</div>
      <div data-name="c" style="height: 40px">C</div>
    </div>`
    list = document.querySelector('.list')
    moved = vi.fn()
    sortable(list, moved)
  })

  afterEach(async () => {
    document.body.innerHTML = ''
    // A drag's guard against the click ending it lasts until the next task: let it go
    await new Promise(resolve => setTimeout(resolve, 0))
  })

  it('is marked as reorderable', () => {
    expect(list.classList.contains('sortable-list')).toBe(true)
  })

  it('moves a row down to where it is dropped, and says from where to where', () => {
    drag(row('a'), below('c'))

    expect(rows()).toEqual(['b', 'c', 'a'])
    expect(moved).toHaveBeenCalledWith(0, 2)
  })

  it('moves a row up', () => {
    drag(row('c'), above('a'))

    expect(rows()).toEqual(['c', 'a', 'b'])
    expect(moved).toHaveBeenCalledWith(2, 0)
  })

  it('marks the row while it is dragged', () => {
    const start = middle(row('a'))
    pointer(row('a'), 'pointerdown', start)
    pointer(document, 'pointermove', { clientX: start.clientX, clientY: below('b') })

    expect(row('a').classList.contains('sorting')).toBe(true)
    expect(list.classList.contains('is-sorting')).toBe(true)

    pointer(document, 'pointerup', {})
    expect(row('a').classList.contains('sorting')).toBe(false)
    expect(list.classList.contains('is-sorting')).toBe(false)
  })

  it('says nothing when a row is dropped back where it was', () => {
    drag(row('b'), middle(row('b')).clientY + 5)

    expect(rows()).toEqual(['a', 'b', 'c'])
    expect(moved).not.toHaveBeenCalled()
  })

  it('leaves a press on a form control to the control', () => {
    drag(row('a').querySelector('input'), below('c'))

    expect(rows()).toEqual(['a', 'b', 'c'])
    expect(moved).not.toHaveBeenCalled()
  })

  it('leaves a touch to scroll the page', () => {
    drag(row('a'), below('c'), { pointerType: 'touch' })

    expect(moved).not.toHaveBeenCalled()
  })

  it('leaves a press with another button alone', () => {
    drag(row('a'), below('c'), { button: 2 })

    expect(moved).not.toHaveBeenCalled()
  })

  it('takes a press that hardly moves as a click, not a drag', () => {
    const click = vi.fn()
    row('b').addEventListener('click', click)

    drag(row('b'), middle(row('b')).clientY + 1)
    row('b').click()

    expect(row('b').classList.contains('sorting')).toBe(false)
    expect(click).toHaveBeenCalled()
  })

  it('swallows the click that ends a drag, but not the next one', async () => {
    const click = vi.fn()
    list.addEventListener('click', click)

    drag(row('a'), below('c'))
    row('a').click()
    expect(click).not.toHaveBeenCalled()

    await new Promise(resolve => setTimeout(resolve, 0))
    row('a').click()
    expect(click).toHaveBeenCalledTimes(1)
  })

  it('drags a row of a list inside a row in its own list only', () => {
    row('b').insertAdjacentHTML('beforeend', `<div class="inner">
      <div data-name="x" style="height: 20px">X</div>
      <div data-name="y" style="height: 20px">Y</div>
    </div>`)
    const inner = row('b').querySelector('.inner')
    const innerMoved = vi.fn()
    sortable(inner, innerMoved)

    drag(row('x'), row('y').getBoundingClientRect().bottom - 1)

    expect([...inner.children].map(each => each.dataset.name)).toEqual(['y', 'x'])
    expect(innerMoved).toHaveBeenCalledWith(0, 1)
    expect(moved).not.toHaveBeenCalled()
    expect(rows()).toEqual(['a', 'b', 'c'])
  })
})
