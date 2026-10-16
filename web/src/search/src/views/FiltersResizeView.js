import Backbone from 'backbone'

// Remembered in this browser, as a convenience: storage can be unavailable, and then each search
// starts from the stylesheet's width
const KEY = 'search.filterWidth'
const MIN = 250
// Of the page's width, so the results are never squeezed out
const MAX_SHARE = 0.6
const STEP = 20

/*
 * The handle between the filters and the results. Dragged, or moved with the arrow keys, it sets the
 * width of the filters, and so of the spatial search map, which is fitted to its new size
 * (SpatialFilterView). The width is the --filter-width the stylesheet lays the column out with.
 */
export default Backbone.View.extend({

  events: {
    pointerdown: 'startDrag',
    pointermove: 'drag',
    pointerup: 'endDrag',
    pointercancel: 'endDrag',
    keydown: 'nudge'
  },

  initialize () {
    if (!this.el) return // a page without the filters
    this.$container = this.$el.parent()
    this.$filters = this.$el.siblings('.filters')
    this.$el.attr({ role: 'separator', 'aria-orientation': 'vertical', 'aria-label': 'Resize the filters', tabindex: 0 })
    let saved = null
    try { saved = Number.parseInt(localStorage.getItem(KEY), 10) } catch (e) {}
    // Otherwise the stylesheet's width stands: in a narrow window the filters are hidden, and measured
    // they would be no width at all
    if (Number.isFinite(saved)) this.setWidth(saved, false)
  },

  width () {
    return Math.round(this.$filters[0].getBoundingClientRect().width)
  },

  setWidth (width, remember = true) {
    const max = Math.round(this.$container[0].getBoundingClientRect().width * MAX_SHARE)
    const clamped = Math.round(Math.min(Math.max(width, MIN), Math.max(max, MIN)))
    this.$container[0].style.setProperty('--filter-width', `${clamped}px`)
    this.$el.attr({ 'aria-valuenow': clamped, 'aria-valuemin': MIN, 'aria-valuemax': max })
    if (remember) {
      try { localStorage.setItem(KEY, String(clamped)) } catch (e) {}
    }
  },

  startDrag (event) {
    const { clientX, pointerId } = event.originalEvent
    this.dragFrom = { x: clientX, width: this.width() }
    // Its moves keep coming to the handle while it is pressed, wherever the pointer goes
    try { this.el.setPointerCapture(pointerId) } catch (e) {}
    this.$container.addClass('resizing-filters')
    event.preventDefault()
  },

  drag (event) {
    if (!this.dragFrom) return
    this.setWidth(this.dragFrom.width + event.originalEvent.clientX - this.dragFrom.x, false)
  },

  endDrag () {
    if (!this.dragFrom) return
    this.dragFrom = null
    this.$container.removeClass('resizing-filters')
    this.setWidth(this.width())
  },

  nudge (event) {
    const step = { ArrowLeft: -STEP, ArrowRight: STEP }[event.key]
    if (!step) return
    this.setWidth(this.width() + step)
    event.preventDefault()
  }
})
