import { afterEach, describe, expect, it, vi } from 'vitest'
import { copyText } from '../copyText'

describe('copyText', () => {
  let input = null

  function makeInput () {
    input = document.createElement('textarea')
    input.value = 'https://example.org/maps/abc?request=GetCapabilities'
    document.body.appendChild(input)
    return input
  }

  afterEach(() => {
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
    if (input) input.remove()
    input = null
  })

  it('writes the value with the clipboard API and resolves true', async () => {
    const writeText = vi.fn().mockResolvedValue()
    vi.stubGlobal('navigator', { clipboard: { writeText } })
    const field = makeInput()

    expect(await copyText(field)).toBe(true)
    expect(writeText).toHaveBeenCalledWith(field.value)
  })

  it('falls back to execCommand when the clipboard API rejects', async () => {
    vi.stubGlobal('navigator', { clipboard: { writeText: vi.fn().mockRejectedValue(new Error('denied')) } })
    const exec = vi.spyOn(document, 'execCommand').mockReturnValue(true)

    expect(await copyText(makeInput())).toBe(true)
    expect(exec).toHaveBeenCalledWith('copy')
  })

  it('resolves false and keeps the text selected when nothing can copy', async () => {
    vi.stubGlobal('navigator', {})
    vi.spyOn(document, 'execCommand').mockReturnValue(false)
    const field = makeInput()

    expect(await copyText(field)).toBe(false)
    expect(field.selectionStart).toBe(0)
    expect(field.selectionEnd).toBe(field.value.length)
  })

  it('resolves false when execCommand throws', async () => {
    vi.stubGlobal('navigator', {})
    vi.spyOn(document, 'execCommand').mockImplementation(() => { throw new Error('unsupported') })

    expect(await copyText(makeInput())).toBe(false)
  })
})
