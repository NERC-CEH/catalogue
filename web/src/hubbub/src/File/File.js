import Backbone from 'backbone'
import { filesize } from 'filesize'

export default Backbone.Model.extend({

  initialize () {
    let action, classes, storage
    const datastore = this.get('datastore')
    if (datastore === 'dropbox') {
      storage = 'dropbox'
    } else if (datastore === 'supporting-documents') {
      storage = 'metadata'
    } else if (datastore === 'eidchub') {
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

    const size = this.has('bytes') ? filesize(this.get('bytes')) : 0
    const date = this.get('lastValidated')
    const hash = this.has('hash') ? this.get('hash') : 'NO_HASH'
    const message = messages[status]
    const moving = status === 'MOVING_FROM' || status === 'MOVING_TO' || status === 'WRITING'
    const validating = status === 'VALIDATING_HASH'

    return this.set({
      action,
      classes,
      date,
      errorType,
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
      date: data.date,
      hash: data.hash,
      message: messages[data.status],
      moving: false,
      path: data.path,
      size: filesize(data.bytes),
      status: data.status
    })
    this.initialize()
  },

  copy (datastore) {
    return new File({
      bytes: this.get('bytes'),
      check: true,
      dataset: this.get('datasetId'),
      datastore,
      path: this.get('path'),
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
  REMOVED_ERROR: {
    content: 'This file failed to be removed'
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
