/*
 * Rows to reorder by dragging, in place of jQuery UI's sortable (dri-one #533). A row is dragged from
 * anywhere in it but its form controls, which keep the pointer for themselves, as jQuery UI's were.
 * Like jQuery UI's, it is for a mouse or pen: a touch still scrolls the page.
 */
const CONTROLS = 'input, textarea, button, select, option'
// How far, in pixels, the pointer must move before a press becomes a drag rather than a click
const THRESHOLD = 3

/**
 * Makes list's children reorderable by dragging.
 *
 * @param {HTMLElement} list
 * @param {function(number, number)} moved called with a row's old and new index once it is dropped
 *   somewhere new
 */
export function sortable (list, moved) {
  list.classList.add('sortable-list')

  list.addEventListener('pointerdown', event => {
    // A row of a list inside a row is dragged in its own list, not with the row around it
    if (event.sortableHandled || event.button !== 0 || event.pointerType === 'touch') return
    const row = [...list.children].find(child => child.contains(event.target))
    if (!row || event.target.closest(CONTROLS)) return
    event.sortableHandled = true

    const from = [...list.children].indexOf(row)
    const startY = event.clientY
    let dragging = false

    const move = moveEvent => {
      if (!dragging) {
        if (Math.abs(moveEvent.clientY - startY) < THRESHOLD) return
        dragging = true
        row.classList.add('sorting')
        list.classList.add('is-sorting')
      }
      moveEvent.preventDefault()
      // Before the first other row whose middle is below the pointer, or else last
      const before = [...list.children].find(other => {
        if (other === row) return false
        const box = other.getBoundingClientRect()
        return moveEvent.clientY < box.top + box.height / 2
      })
      if (before) {
        if (row.nextElementSibling !== before) list.insertBefore(row, before)
      } else if (list.lastElementChild !== row) {
        list.append(row)
      }
    }

    const drop = () => {
      document.removeEventListener('pointermove', move)
      document.removeEventListener('pointerup', drop)
      document.removeEventListener('pointercancel', drop)
      if (!dragging) return
      row.classList.remove('sorting')
      list.classList.remove('is-sorting')
      // The click that ends a drag isn't one on whatever the row was dropped over. It comes straight
      // after the pointerup, if at all, so the guard goes after this task: left any longer, it
      // would swallow the next real click
      const swallow = clickEvent => {
        clickEvent.stopPropagation()
        clickEvent.preventDefault()
      }
      document.addEventListener('click', swallow, { capture: true, once: true })
      setTimeout(() => document.removeEventListener('click', swallow, { capture: true }), 0)
      const to = [...list.children].indexOf(row)
      if (to !== from) moved(from, to)
    }

    document.addEventListener('pointermove', move)
    document.addEventListener('pointerup', drop)
    document.addEventListener('pointercancel', drop)
  })
}
