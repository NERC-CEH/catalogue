import Backbone from 'backbone'
import Filesize from 'filesize'

export default Backbone.Model.extend({

  initialize () {
    let action, classes, date, storage
    const path = this.get('path')
    if (path.startsWith('/dropbox/')) {
      storage = 'dropbox'
    } else if (path.startsWith('/supporting-documents/')) {
      storage = 'metadata'
    } else if (path.startsWith('/eidchub/')) {
      storage = 'datastore'
    }

    const status = this.get('status')
    const errorType = status in errorTypes ? errorTypes[status] : 'valid'
    const open = !validTypes.has(status)

    if (open && (errorType !== 'valid')) {
      classes = 'panel-danger'
    } else if (open) {
      classes = 'panel-default'
    } else {
      classes = 'panel-default is-collapsed'
    }

    if (errorType === 'valid') {
      action = keyToAction[storage]
    } else {
      action = errorToAction[status]
    }

    const size = this.has('bytes') ? Filesize(this.get('bytes')) : 0
    if (this.has('time')) { date = simpleDate(this.get('time')) }
    const hash = this.has('hash') ? this.get('hash') : 'NO_HASH'
    const estimate = sizeToTime(this.get('bytes'))
    const message = messages[status]
    const moving = status.includes('MOVING') || (status === 'WRITING')
    const validating = status === 'VALIDATING_HASH'

    return this.set({
      action,
      classes,
      date,
      errorType,
      estimate,
      hash,
      message,
      moving,
      open,
      size,
      validating
    })
  },

  update (data) {
    this.set({
      check: false,
      date: simpleDate(data.time),
      estimate: sizeToTime(data.bytes),
      hash: data.hash,
      message: messages[status],
      moving: false,
      size: Filesize(data.bytes),
      status: data.status
    })
    return this.initialize()
  },

  copy (path) {
    return new File({
      bytes: this.get('bytes'),
      check: true,
      name: this.get('name'),
      path: this.get('path').replace(/^\/(dropbox|eidchub|supporting-documents)\//, path),
      status: 'MOVING_TO'
    })
  }
})

const errorTypes = {
  CHANGED_HASH: 'hash',
  NO_HASH: 'hash',
  CHANGED_MTIME: 'file',
  UNKNOWN: 'file',
  UNKNOWN_MISSING: 'file',
  MISSING: 'file',
  MISSING_UNKNOWN: 'file',
  MOVED_UNKNOWN: 'file',
  MOVED_UNKNOWN_MISSING: 'file',
  REMOVED_UNKNOWN: 'file',
  ZIPPED_UNKNOWN: 'file',
  ZIPPED_UNKNOWN_MISSING: 'file',
  INVALID: 'file'
}

const keyToAction = {
  datastore: 'move-metadata',
  dropbox: 'move-both',
  metadata: 'move-datastore'
}

const errorToAction = {
  CHANGED_HASH: 'accept',
  MOVING_FROM_ERROR: 'accept',
  MOVING_TO_ERROR: 'ignore',
  NO_HASH: 'validate',
  CHANGED_MTIME: 'validate',
  UNKNOWN: 'accept',
  UNKNOWN_MISSING: 'ignore',
  MISSING: 'ignore',
  MISSING_UNKNOWN: 'accept',
  MOVED_UNKNOWN: 'accept',
  MOVED_UNKNOWN_MISSING: 'ignore',
  VALIDATING_HASH: 'validate',
  REMOVED_UNKNOWN: 'accept',
  ZIPPED_UNKNOWN: 'accept',
  ZIPPED_UNKNOWN_MISSING: 'ignore'
}

const messages = {
  CHANGED_HASH: {
    content: 'THIS FILE HAS CHANGED !'
  },
  CHANGED_MTIME: {
    title: 'Detected a possible file change',
    content: `\
THIS FILE MAY HAVE CHANGED. 
Please VALIDATE to ensure the file has not been altered or corrupted\
`
  },
  INVALID: {
    content: 'Something went wrong with this file'
  },
  MISSING: {
    content: 'THIS FILE IS MISSING'
  },
  MISSING_UNKNOWN: {
    content: 'This file was previously marked as missing but it has now been re-added'
  },
  MOVED_UNKNOWN: {
    content: 'This file was previously marked as moved to a different location but it has been re-added here'
  },
  MOVED_UNKNOWN_MISSING: {
    content: 'This file was previously marked as moved to a different location, then re-added, but is now missing'
  },
  MOVING_FROM: {
    content: 'The file is currently being moved'
  },
  MOVING_FROM_ERROR: {
    content: 'The file failed to move'
  },
  MOVING_TO: {
    content: 'The file is currently being moved'
  },
  MOVING_TO_ERROR: {
    content: 'The file failed to move'
  },
  NO_HASH: {
    content: 'This file has not yet been validated or has failed to validate'
  },
  REMOVED_UNKNOWN: {
    content: 'This file was previously deleted but has now been re-added'
  },
  VALIDATING_HASH: {
    content: 'This file is currently being validated, please wait. Large files may take a long time to process'
  },
  UNKNOWN: {
    content: 'Unknown file'
  },
  UNKNOWN_MISSING: {
    content: 'This file was marked as unknown and has now been deleted'
  },
  WRITING: {
    content: 'The file is currently being written to disk'
  },
  ZIPPED_UNKNOWN: {
    content: 'This file was zipped and has been manually added'
  },
  ZIPPED_UNKNOWN_MISSING: {
    content: 'This file was zipped, was been manually added, now manually removed'
  }
}

const validTypes = new Set([
  'MOVING_FROM',
  'MOVING_TO',
  'NO_HASH',
  'VALID',
  'VALIDATING_HASH',
  'WRITING'
])

// calculated using Hubbub on the SAN, 22/05/19, recalculate for better estimates
const timeEstimate = {
  1400000: '1s',
  6300000: '2s',
  10000000: '3s',
  12000000: '4s',
  24000000: '5s',
  39000000: '6s',
  50000000: '8s',
  54000000: '9s',
  69000000: '10s',
  79000000: '20s',
  170000000: '30s',
  220000000: '40s',
  250000000: '50s',
  290000000: '1m',
  320000000: '1m10s',
  400000000: '1m20s',
  470000000: '1m30s',
  730000000: '2m',
  800000000: '2m30s',
  1300000000: '5m',
  1600000000: '7m',
  4300000000: '8m',
  4700000000: '10m',
  5000000000: '12m',
  5300000000: '14m',
  12000000000: '45m',
  18000000000: '1h',
  46000000000: '2h',
  65000000000: '2h+'
}

const simpleDate = function (time) {
  const date = new Date(time)
  const d = date.getDate()
  const M = date.getMonth() + 1
  const y = ('' + date.getFullYear()).slice(2)

  let h = date.getHours()
  if (h < 10) { h = `0${h}` }

  let m = date.getMinutes()
  if (m < 10) { m = `0${m}` }

  return `${d}/${M}/${y} - ${h}:${m}`
}

const sizeToTime = function (size) {
  let time
  for (const key in timeEstimate) {
    const value = timeEstimate[key]
    if (size < key) {
      time = value
      break
    }
  }
  return time
}
