import { Modal } from 'bootstrap'

// The catalogue's dialogs, on Bootstrap's modal (NERC-CEH/dri-one#531). They replace sweetalert2 and
// the browser's own window.confirm/alert, so every page asks and tells in the same way.
//
// A dialog's words are text, never HTML: `text` is a string, or a list of them, one paragraph each, put
// on the page as text. Anything richer, such as the editor's conflict dialog with its textarea, is
// given as `body`, DOM nodes the caller has built. So no call site has to remember to escape what it
// shows, and nothing it shows can become markup.
//
// Each resolves { isConfirmed }: true only when the confirm button was pressed, false when the dialog
// was cancelled or dismissed (Escape, the close button, a click outside it).
//
// The callers use the `dialog` object rather than importing its functions, so a spec can spy on them.

// Font Awesome's classes in full, so the build's icon subset sees them (scripts/font-awesome-icons.js)
const ICONS = {
  success: 'fa-solid fa-circle-check text-success',
  error: 'fa-solid fa-circle-xmark text-danger',
  warning: 'fa-solid fa-triangle-exclamation text-warning',
  info: 'fa-solid fa-circle-info text-info'
}

// Bootstrap shows one modal at a time, and ignores a show() while another modal is still opening or
// closing, so a dialog asked for while one is open waits for it to close rather than being lost.
let queue = Promise.resolve()
let count = 0

function element (tag, className, text) {
  const node = document.createElement(tag)
  if (className) node.className = className
  if (text !== undefined) node.textContent = text
  return node
}

function button (className, text) {
  const node = element('button', `btn ${className}`, text)
  node.type = 'button'
  return node
}

function build ({ title, text = [], body = [], icon, confirmText, cancelText, danger }) {
  const id = `dialog-title-${++count}`
  const modal = element('div', 'modal fade')
  modal.tabIndex = -1
  modal.setAttribute('aria-labelledby', id)
  modal.setAttribute('aria-hidden', 'true')

  const header = element('div', 'modal-header')
  const heading = element('h2', 'modal-title h5')
  heading.id = id
  if (ICONS[icon]) {
    const symbol = element('i', `${ICONS[icon]} me-2`)
    symbol.setAttribute('aria-hidden', 'true')
    heading.append(symbol)
  }
  heading.append(title)
  const close = button('btn-close', undefined)
  close.setAttribute('aria-label', 'Close')
  close.dataset.bsDismiss = 'modal'
  header.append(heading, close)

  const content = element('div', 'modal-body')
  content.append(...[text].flat().map(paragraph => element('p', null, paragraph)), ...[body].flat())
  content.querySelector('p:last-child')?.classList.add('mb-0')

  const footer = element('div', 'modal-footer')
  let cancel = null
  if (cancelText) {
    cancel = button('btn-secondary', cancelText)
    cancel.dataset.bsDismiss = 'modal'
    footer.append(cancel)
  }
  const confirm = button(danger ? 'btn-danger' : 'btn-primary', confirmText)
  confirm.dataset.dialogConfirm = ''
  footer.append(confirm)

  const dialog = element('div', 'modal-dialog modal-dialog-centered modal-dialog-scrollable')
  const frame = element('div', 'modal-content')
  frame.append(header, content, footer)
  dialog.append(frame)
  modal.append(dialog)
  // Enter answers the dialog, as it did with the browser's own, but not with something that can't be
  // undone, such as deleting a record: that waits for the button to be chosen
  return { modal, confirm, initial: danger && cancel ? cancel : confirm }
}

function show (options) {
  return new Promise(resolve => {
    const { modal, confirm, initial } = build(options)
    // Bootstrap gives focus back only to a modal's data-bs-toggle trigger, and these are opened from
    // code, so a keyboard user would be left at the top of the page; it goes back where it was
    const opener = document.activeElement
    let isConfirmed = false
    confirm.addEventListener('click', () => {
      isConfirmed = true
      instance.hide()
    })
    modal.addEventListener('shown.bs.modal', () => initial.focus())
    modal.addEventListener('hidden.bs.modal', () => {
      instance.dispose()
      modal.remove()
      if (opener?.isConnected) opener.focus()
      resolve({ isConfirmed })
    })
    document.body.append(modal)
    const instance = new Modal(modal)
    instance.show()
  })
}

function enqueue (options) {
  const shown = queue.then(() => show(options))
  queue = shown
  return shown
}

export const dialog = {
  // Asks before something is done: resolves { isConfirmed: true } only when the user agrees
  confirm ({ confirmText = 'OK', cancelText = 'Cancel', icon = 'warning', ...options }) {
    return enqueue({ ...options, icon, confirmText, cancelText })
  },

  // Tells the user something: resolves once the dialog is closed, however it was
  alert ({ confirmText = 'Close', ...options }) {
    return enqueue({ ...options, confirmText })
  }
}
