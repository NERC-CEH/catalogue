import { describe, expect, it } from 'vitest'
import { File } from '../../src/File'

describe('File model', function () {
  const attributes = {
    bytes: 300,
    datasetId: 'bf163866-3baf-4dcb-a99a-45a98c120027',
    datastore: 'eidchub',
    format: 'csv',
    hash: '28111a4e084f1a57c7a0f50bc9a35472',
    hashingTime: 2.34,
    lastModified: '2000-01-12T23:34:57',
    lastValidated: '2022-03-05T06:23:57',
    path: 'data.csv',
    status: 'VALID'
  }

  it('initializes', function () {
    // given

    // when
    const file = new File(attributes)

    // then
    // set or manipulated in initialize
    expect(file.get('action')).toBe('move-metadata')
    expect(file.get('message')).toBe(undefined)
    expect(file.get('moving')).toBe(false)
    expect(file.get('open')).toBe(false)
    expect(file.get('size')).toBe('300 B')

    // from server
    expect(file.has('bytes')).toBe(true)
    expect(file.has('datasetId')).toBe(true)
    expect(file.has('datastore')).toBe(true)
    expect(file.has('format')).toBe(true)
    expect(file.has('hashingTime')).toBe(true)
    expect(file.has('lastModified')).toBe(true)
    expect(file.has('lastValidated')).toBe(true)
    expect(file.has('path')).toBe(true)
  })

  it('updates', function () {
    // given
    const file = new File(attributes)

    // when
    file.update(attributes)

    // then
    expect(file.get('check')).toBe(false)
    expect(file.get('moving')).toBe(false)
  })

  it('copy', function () {
    // given
    const file = new File(attributes)

    // when
    const copy = file.copy('eidchub')

    // then
    expect(copy.get('check')).toBe(true)
    expect(copy.get('datastore')).toBe('eidchub')
    expect(copy.get('status')).toBe('MOVING_TO')
  })
})
