import { describe, expect, it } from 'vitest'
import { formatBytes } from '../formatBytes'

// One convention for every size the pages show: binary multiples with the familiar unit names,
// as filesize's jedec standard wrote them (dri-one #532)
describe('Formatting a file size', () => {
  it('shows bytes below a kilobyte as they are', () => {
    expect(formatBytes(0)).toBe('0 B')
    expect(formatBytes(1)).toBe('1 B')
    expect(formatBytes(1023)).toBe('1023 B')
  })

  it('counts a kilobyte as 1024 bytes', () => {
    expect(formatBytes(1000)).toBe('1000 B')
    expect(formatBytes(1024)).toBe('1 KB')
    expect(formatBytes(1536)).toBe('1.5 KB')
  })

  it('rounds to at most two decimal places', () => {
    expect(formatBytes(123456789)).toBe('117.74 MB')
    expect(formatBytes(5e9)).toBe('4.66 GB')
  })

  it('moves up a unit rather than showing 1024 of one', () => {
    expect(formatBytes(1024 * 1024 - 1)).toBe('1 MB')
  })

  it('goes up to terabytes', () => {
    expect(formatBytes(1024 ** 4)).toBe('1 TB')
    expect(formatBytes(1024 ** 5)).toBe('1024 TB')
  })

  it('shows no size for one it was not given', () => {
    expect(formatBytes(undefined)).toBe('0 B')
    expect(formatBytes(-1)).toBe('0 B')
  })
})
