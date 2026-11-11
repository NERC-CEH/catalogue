import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { autocomplete, autocompleteOf } from '../src/utils/autocomplete'

// The editor's look-up box, which replaced jQuery UI's autocomplete (dri-one #533)
describe('A look-up box', () => {
  const settle = () => new Promise(resolve => setTimeout(resolve, 0))
  const key = (input, name) => {
    const event = new KeyboardEvent('keydown', { key: name, bubbles: true, cancelable: true })
    input.dispatchEvent(event)
    return event
  }
  const offered = menu => [...menu.querySelectorAll('[role=option]')].map(option => option.textContent)
  const ITEMS = [{ label: 'Soil', value: 'soil' }, { label: 'Soil moisture', value: 'soil-moisture' }]
  let input

  beforeEach(() => {
    document.body.innerHTML = '<form><input type="text"></form>'
    input = document.querySelector('input')
  })

  afterEach(() => { document.body.innerHTML = '' })

  it('is a combobox to assistive technology, controlling its list', () => {
    const { menu } = autocomplete(input, { source: [] })

    expect(input.getAttribute('role')).toBe('combobox')
    expect(input.getAttribute('aria-autocomplete')).toBe('list')
    expect(input.getAttribute('aria-expanded')).toBe('false')
    expect(input.getAttribute('aria-controls')).toBe(menu.id)
    expect(input.getAttribute('autocomplete')).toBe('off')
    expect(menu.getAttribute('role')).toBe('listbox')
    expect(autocompleteOf(input).menu).toBe(menu)
  })

  it('searches what has been typed, once typing pauses', async () => {
    vi.useFakeTimers()
    const source = vi.fn(() => ITEMS)
    autocomplete(input, { source, delay: 300 })

    input.value = 'so'
    input.dispatchEvent(new Event('input'))
    input.value = 'soi'
    input.dispatchEvent(new Event('input'))
    await vi.advanceTimersByTimeAsync(299)
    expect(source).not.toHaveBeenCalled()
    await vi.advanceTimersByTimeAsync(1)

    expect(source).toHaveBeenCalledTimes(1)
    expect(source).toHaveBeenCalledWith('soi')
    vi.useRealTimers()
  })

  it('offers what the search finds, in a list beneath the field', async () => {
    const { search, menu } = autocomplete(input, { source: async () => ITEMS })

    await search('soil')

    expect(menu.isConnected).toBe(true)
    expect(menu.classList.contains('dropdown-menu')).toBe(true)
    expect(offered(menu)).toEqual(['Soil', 'Soil moisture'])
    expect(input.getAttribute('aria-expanded')).toBe('true')
    expect(document.querySelector('[role=status]').textContent).toBe('2 results')
  })

  it('doesn\'t search a term shorter than its minimum', async () => {
    const source = vi.fn(() => ITEMS)
    const { search, menu } = autocomplete(input, { source, minLength: 2 })

    await search('s')

    expect(source).not.toHaveBeenCalled()
    expect(menu.isConnected).toBe(false)
  })

  it('searches an empty field when its minimum is nought', async () => {
    const source = vi.fn(() => ITEMS)
    const { search } = autocomplete(input, { source, minLength: 0 })

    await search('')

    expect(source).toHaveBeenCalledWith('')
  })

  it('filters a list of items by what is typed, ignoring case', async () => {
    const { search, menu } = autocomplete(input, { source: ITEMS })

    await search('MOIST')

    expect(offered(menu)).toEqual(['Soil moisture'])
  })

  it('closes when nothing matches', async () => {
    const { search, menu } = autocomplete(input, { source: async () => [] })

    await search('peat')

    expect(menu.isConnected).toBe(false)
    expect(document.querySelector('[role=status]').textContent).toBe('0 results')
  })

  it('puts the item chosen in the field, then says which it was', async () => {
    const select = vi.fn(() => expect(input.value).toBe('soil-moisture'))
    const { search, menu } = autocomplete(input, { source: async () => ITEMS, select })
    await search('soil')

    menu.querySelectorAll('[role=option]')[1].click()

    expect(select).toHaveBeenCalledWith(ITEMS[1], expect.any(Event))
    expect(input.value).toBe('soil-moisture')
    expect(menu.isConnected).toBe(false)
  })

  it('puts an item\'s label in the field when it has no value', async () => {
    const { search, menu } = autocomplete(input, { source: async () => [{ label: 'NERC', url: 'https://ror.org/02b5d8509' }] })
    await search('ne')

    menu.querySelector('[role=option]').click()

    expect(input.value).toBe('NERC')
  })

  it('shows an item as its caller renders it', async () => {
    const render = item => {
      const div = document.createElement('div')
      div.innerHTML = `${item.label} <span>${item.value}</span>`
      return div
    }
    const { search, menu } = autocomplete(input, { source: async () => ITEMS, render, menuClass: 'special' })
    await search('soil')

    expect(menu.classList.contains('special')).toBe(true)
    expect(menu.querySelector('[role=option] span').textContent).toBe('soil')
  })

  describe('a notice', () => {
    const NOTICE = [{ notice: 'No matching records' }]

    it('is read, not chosen', async () => {
      const select = vi.fn()
      const { search, menu } = autocomplete(input, { source: async () => NOTICE, select })
      await search('peat')

      const notice = menu.querySelector('[role=option]')
      expect(notice.textContent).toBe('No matching records')
      expect(notice.getAttribute('aria-disabled')).toBe('true')
      expect(notice.classList.contains('autocomplete-notice')).toBe(true)
      notice.click()

      expect(select).not.toHaveBeenCalled()
      expect(input.value).toBe('')
      expect(document.querySelector('[role=status]').textContent).toBe('0 results')
    })

    it('is passed over by the arrow keys', async () => {
      const { search } = autocomplete(input, { source: async () => [...NOTICE, ...ITEMS] })
      await search('soil')

      key(input, 'ArrowDown')

      expect(document.getElementById(input.getAttribute('aria-activedescendant')).textContent).toBe('Soil')
    })
  })

  describe('from the keyboard', () => {
    let select, controller
    const activeText = () => document.getElementById(input.getAttribute('aria-activedescendant'))?.textContent

    beforeEach(async () => {
      select = vi.fn()
      controller = autocomplete(input, { source: async () => ITEMS, select })
      await controller.search('soil')
    })

    it('moves through the matches with the arrow keys, back to the field past either end', () => {
      expect(key(input, 'ArrowDown').defaultPrevented).toBe(true)
      expect(activeText()).toBe('Soil')
      expect(controller.menu.querySelector('.active').textContent).toBe('Soil')
      key(input, 'ArrowDown')
      expect(activeText()).toBe('Soil moisture')
      key(input, 'ArrowDown')
      expect(input.hasAttribute('aria-activedescendant')).toBe(false)
      key(input, 'ArrowUp')
      expect(activeText()).toBe('Soil moisture')
    })

    it('chooses with Enter, rather than submitting the form', () => {
      key(input, 'ArrowDown')

      expect(key(input, 'Enter').defaultPrevented).toBe(true)
      expect(select).toHaveBeenCalledWith(ITEMS[0], expect.any(Event))
      expect(input.value).toBe('soil')
    })

    it('leaves Enter to the form when nothing is picked out', () => {
      expect(key(input, 'Enter').defaultPrevented).toBe(false)
      expect(select).not.toHaveBeenCalled()
    })

    it('closes with Escape', () => {
      key(input, 'Escape')

      expect(controller.menu.isConnected).toBe(false)
      expect(input.getAttribute('aria-expanded')).toBe('false')
    })

    it('searches again with the down arrow once closed', async () => {
      key(input, 'Escape')
      input.value = 'soil'

      key(input, 'ArrowDown')
      await settle()

      expect(controller.menu.isConnected).toBe(true)
    })
  })

  describe('a slow search', () => {
    const deferred = () => {
      let resolve
      const promise = new Promise(_resolve => { resolve = _resolve })
      return { promise, resolve }
    }

    it('shows that it is searching, until it is answered', async () => {
      const answer = deferred()
      const { search } = autocomplete(input, { source: () => answer.promise })

      const searching = search('soil')
      expect(input.getAttribute('aria-busy')).toBe('true')
      answer.resolve(ITEMS)
      await searching

      expect(input.hasAttribute('aria-busy')).toBe(false)
    })

    it('stops searching when the search fails, rather than spinning', async () => {
      vi.spyOn(console, 'error').mockImplementation(() => {})
      const { search, menu } = autocomplete(input, { source: async () => { throw new Error('503') } })

      await search('soil')

      expect(input.hasAttribute('aria-busy')).toBe(false)
      expect(menu.isConnected).toBe(false)
    })

    it('can\'t replace the answer to a later search', async () => {
      const first = deferred()
      const second = deferred()
      const answers = [first, second]
      const { search, menu } = autocomplete(input, { source: () => answers.shift().promise })

      const earlier = search('so')
      const later = search('soil m')
      second.resolve([ITEMS[1]])
      await later
      first.resolve(ITEMS)
      await earlier

      expect(offered(menu)).toEqual(['Soil moisture'])
    })

    it('isn\'t shown once the field is left', async () => {
      const answer = deferred()
      const { search, menu } = autocomplete(input, { source: () => answer.promise })

      const searching = search('soil')
      input.dispatchEvent(new FocusEvent('blur'))
      answer.resolve(ITEMS)
      await searching

      expect(menu.isConnected).toBe(false)
      expect(input.hasAttribute('aria-busy')).toBe(false)
    })
  })

  it('keeps the focus in the field when a match is pressed', async () => {
    const { search, menu } = autocomplete(input, { source: async () => ITEMS })
    await search('soil')

    const press = new MouseEvent('mousedown', { bubbles: true, cancelable: true })
    menu.querySelector('[role=option]').dispatchEvent(press)

    expect(press.defaultPrevented).toBe(true)
  })

  it('picks out the match under the mouse', async () => {
    const { search, menu } = autocomplete(input, { source: async () => ITEMS })
    await search('soil')

    menu.querySelectorAll('[role=option]')[1].dispatchEvent(new MouseEvent('mousemove', { bubbles: true }))

    expect(input.getAttribute('aria-activedescendant')).toBe(menu.querySelectorAll('[role=option]')[1].id)
  })

  it('closes when the field is left', async () => {
    const { search, menu } = autocomplete(input, { source: async () => ITEMS })
    await search('soil')

    input.dispatchEvent(new FocusEvent('blur'))

    expect(menu.isConnected).toBe(false)
  })
})
