import { afterEach, describe, expect, it } from 'vitest'
import $ from 'jquery'
import { linkHref } from '../src/CatalogueApp'

describe('linkHref', () => {
  afterEach(() => $(document.body).empty())

  function click (markup, selector) {
    const $control = $(markup).appendTo(document.body)
    let href
    $control.on('click', event => { event.preventDefault(); href = linkHref(event) })
    $control.find(selector).addBack(selector).first()[0].click()
    return href
  }

  it('reads the link the control is', () => {
    expect(click('<a class="catalogue-control" href="/documents/1/catalogue">Move</a>', 'a')).toBe('/documents/1/catalogue')
  })

  it('reads the link when the click lands on its icon', () => {
    expect(click('<a class="catalogue-control" href="/documents/1/catalogue"><i class="fa-solid fa-right-from-bracket"></i> Move</a>', 'i'))
      .toBe('/documents/1/catalogue')
  })

  it('reads the link inside a span control', () => {
    expect(click('<span class="catalogue-control"><a href="/documents/1/catalogue"><i></i> Move</a></span>', 'i')).toBe('/documents/1/catalogue')
  })
})
