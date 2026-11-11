/*
 * A look-up box: as the editor types, a list of matches beneath the field to choose from, in place of
 * jQuery UI's autocomplete (dri-one #533). The list is a Bootstrap dropdown menu, and the field a
 * combobox to assistive technology: the arrow keys move through the matches, Enter chooses one and
 * Escape closes the list.
 *
 * An item is { label, value } plus anything its caller wants back when it is chosen. Choosing one
 * writes its value (or, without one, its label) into the field, as jQuery UI did, then calls select.
 * An item with a notice is a line to read that can't be chosen: "No matching records".
 */
const controllers = new WeakMap()
let menus = 0

// Says how many matches there are as they arrive, as jQuery UI's hidden live region did. One for the
// page, in its body: beside a field, it would be the last child of an .input-group, squaring the
// field's rounded corners
let liveRegion = null
const announce = message => {
  if (!liveRegion?.isConnected) {
    liveRegion = document.createElement('div')
    liveRegion.className = 'visually-hidden'
    liveRegion.setAttribute('role', 'status')
    document.body.append(liveRegion)
  }
  liveRegion.textContent = message
}

/**
 * Makes input a look-up box.
 *
 * @param {HTMLInputElement} input
 * @param {object} options
 * @param {Array|function(string): (Array|Promise<Array>)} options.source the items, filtered by what
 *   is typed, or a function from the term typed (trimmed by its caller as it needs) to the items
 * @param {number} [options.minLength=1] the shortest term searched for
 * @param {number} [options.delay=300] how long typing must pause, in ms, before a search
 * @param {function(object): (string|Node)} [options.render] an item's content: its label, as text, by default
 * @param {function(object, Event)} [options.select] called with the item chosen, once it is in the field
 * @param {string} [options.menuClass] a class for the list, to style one look-up's apart
 * @returns {{search: function(string=): Promise<void>, close: function(), menu: HTMLElement}}
 */
export function autocomplete (input, { source, minLength = 1, delay = 300, render, select, menuClass } = {}) {
  const id = `autocomplete-${++menus}`
  const menu = document.createElement('ul')
  menu.id = id
  menu.className = ['dropdown-menu', 'show', 'autocomplete-menu', menuClass].filter(Boolean).join(' ')
  menu.setAttribute('role', 'listbox')

  input.setAttribute('role', 'combobox')
  input.setAttribute('aria-autocomplete', 'list')
  input.setAttribute('aria-expanded', 'false')
  input.setAttribute('aria-controls', id)
  input.setAttribute('autocomplete', 'off')

  let items = []
  let active = -1
  let timer = null
  // Each search is numbered, so a slow answer to an earlier one can't replace a later one's, and
  // one the field has given up on (by being left, or emptied) isn't shown at all
  let searches = 0
  const cancel = () => {
    clearTimeout(timer)
    searches++
    input.removeAttribute('aria-busy')
  }

  const options = () => [...menu.children]
  const choosable = index => index >= 0 && index < items.length && !items[index].notice

  const activate = index => {
    active = index
    options().forEach((option, i) => {
      option.classList.toggle('active', i === index)
      option.setAttribute('aria-selected', String(i === index))
    })
    if (index < 0) {
      input.removeAttribute('aria-activedescendant')
    } else {
      const option = menu.children[index]
      input.setAttribute('aria-activedescendant', option.id)
      option.scrollIntoView({ block: 'nearest' })
    }
  }

  const close = () => {
    menu.remove()
    input.setAttribute('aria-expanded', 'false')
    activate(-1)
  }

  // Beneath the field, at least as wide as it, on the page so a scrolled container can't clip it
  const place = () => {
    const box = input.getBoundingClientRect()
    menu.style.top = `${box.bottom + window.scrollY}px`
    menu.style.left = `${box.left + window.scrollX}px`
    menu.style.minWidth = `${box.width}px`
  }

  const show = found => {
    items = found
    menu.replaceChildren(...found.map((item, i) => {
      const option = document.createElement('li')
      option.id = `${id}-${i}`
      option.className = 'dropdown-item'
      option.setAttribute('role', 'option')
      option.setAttribute('aria-selected', 'false')
      if (item.notice) {
        option.classList.add('disabled', 'autocomplete-notice')
        option.setAttribute('aria-disabled', 'true')
        option.textContent = item.notice
      } else if (render) {
        option.append(render(item))
      } else {
        option.textContent = item.label
      }
      return option
    }))
    const matches = found.filter(item => !item.notice).length
    announce(`${matches} ${matches === 1 ? 'result' : 'results'}`)
    if (!found.length) {
      close()
      return
    }
    place()
    document.body.append(menu)
    input.setAttribute('aria-expanded', 'true')
    activate(-1)
  }

  const choose = (index, event) => {
    if (!choosable(index)) return
    const item = items[index]
    input.value = item.value ?? item.label
    close()
    select?.(item, event)
  }

  const lookUp = async term => {
    if (Array.isArray(source)) {
      const wanted = term.toLowerCase()
      return source.filter(item => String(item.label ?? item.value).toLowerCase().includes(wanted))
    }
    return source(term)
  }

  const search = async (term = input.value) => {
    clearTimeout(timer)
    if (term.length < minLength) {
      cancel()
      close()
      return
    }
    const search = ++searches
    input.setAttribute('aria-busy', 'true')
    let found
    try {
      found = await lookUp(term)
    } catch (error) {
      console.error('Look-up failed:', error)
      found = []
    }
    if (search !== searches) return
    input.removeAttribute('aria-busy')
    // The view may have been re-rendered, replacing the field, while the search was answered
    if (input.isConnected) show(found ?? [])
  }

  input.addEventListener('input', () => {
    clearTimeout(timer)
    timer = setTimeout(() => search(), delay)
  })

  input.addEventListener('keydown', event => {
    const open = menu.isConnected
    const step = direction => {
      if (!open) {
        search()
        return
      }
      // Past either end the field itself is active again, as in jQuery UI; notices are passed over
      let index = active
      do {
        index += direction
        if (index < -1) index = items.length - 1
        if (index >= items.length) index = -1
      } while (index !== -1 && !choosable(index))
      activate(index)
    }
    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault()
        step(1)
        break
      case 'ArrowUp':
        event.preventDefault()
        step(-1)
        break
      case 'Enter':
        // Chooses, rather than submitting the form the field is in
        if (open && choosable(active)) {
          event.preventDefault()
          choose(active, event)
        }
        break
      case 'Escape':
        if (open) {
          event.preventDefault()
          close()
        }
        break
    }
  })

  input.addEventListener('blur', () => {
    cancel()
    close()
  })

  // Pressing on the list would take the focus from the field, closing the list before the click
  menu.addEventListener('mousedown', event => event.preventDefault())
  menu.addEventListener('click', event => {
    const option = event.target.closest('[role=option]')
    if (option) choose(options().indexOf(option), event)
  })
  menu.addEventListener('mousemove', event => {
    const index = options().indexOf(event.target.closest('[role=option]'))
    if (index !== active && choosable(index)) activate(index)
  })

  const controller = { search, close, menu }
  controllers.set(input, controller)
  return controller
}

/** The look-up box made of input, if it is one */
export const autocompleteOf = input => controllers.get(input?.jquery ? input[0] : input)
