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
    expect(file.get('classes')).toBe('panel-default is-collapsed')
    expect(file.get('date')).toBe('2022-03-05T06:23:57')
    expect(file.get('errorType')).toBe('valid')
    expect(file.get('hash')).toBe('28111a4e084f1a57c7a0f50bc9a35472')
    expect(file.get('message')).toBe(undefined)
    expect(file.get('moving')).toBe(false)
    expect(file.get('open')).toBe(false)
    expect(file.get('size')).toBe('300 B')
    expect(file.get('validating')).toBe(false)

    // from server
    expect(file.has('bytes')).toBeTrue()
    expect(file.has('datasetId')).toBeTrue()
    expect(file.has('datastore')).toBeTrue()
    expect(file.has('format')).toBeTrue()
    expect(file.has('hashingTime')).toBeTrue()
    expect(file.has('lastModified')).toBeTrue()
    expect(file.has('lastValidated')).toBeTrue()
    expect(file.has('path')).toBeTrue()
  })

  it('updates', function () {
    // given
    const file = new File(attributes)

    // when
    file.update(attributes)

    // then
    expect(file.get('check')).toBeFalse()
    expect(file.get('moving')).toBeFalse()
  })

  it('copy', function () {
    // given
    const file = new File(attributes)

    // when
    const copy = file.copy('eidchub')

    // then
    expect(copy.get('check')).toBeTrue()
    expect(copy.get('datastore')).toBe('eidchub')
    expect(copy.get('status')).toBe('MOVING_TO')
  })
})
