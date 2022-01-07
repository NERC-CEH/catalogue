/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'filesize'
], function(Backbone, filesize) {

  let File;
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
  };

  const keyToAction = {
    datastore: 'move-metadata',
    dropbox: 'move-both',
    metadata: 'move-datastore'
  };

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
  };

  const messages = {
    CHANGED_HASH: {
      content: 'The file has changed'
    },
    CHANGED_MTIME: {
      title: 'Detected a possible file change',
      content: `\
The meta information about this file has changed
This could be as small as someone opening the file and saving it
Or it could mean the file contents have changed
Please "VALIDATE" this file then resolve any new errors\
`
    },
    INVALID: {
      content: 'Something went wrong with this file'
    },
    MISSING: {
      content: 'This file is missing'
    },
    MISSING_UNKNOWN: {
      content: 'This file was missing, but has been added manually'
    },
    MOVED_UNKNOWN: {
      content: 'This file was moved, but has been added manually'
    },
    MOVED_UNKNOWN_MISSING: {
      content: 'This file was moved, then added manually, now removed manually'
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
      content: 'This file has not been validated or failed to validate'
    },
    REMOVED_UNKNOWN: {
      content: 'This file, previously removed, has now been manually added'
    },
    VALIDATING_HASH: {
      content: 'This file is currently being validated, large files take a long time to process'
    },
    UNKNOWN: {
      content: 'This is an unknown file'
    },
    UNKNOWN_MISSING: {
      content: 'This was an unknown file, but has been removed manually'
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
  };

  const validTypes = new Set([
    "MOVING_FROM",
    "MOVING_TO",
    "NO_HASH",
    "VALID",
    "VALIDATING_HASH",
    "WRITING"
  ]);

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
  };

  const simpleDate = function(time) {
    const date = new Date(time);
    const d = date.getDate();
    const M = date.getMonth() + 1;
    const y = ('' + date.getFullYear()).slice(2);

    let h = date.getHours();
    if (h < 10) { h = `0${h}`; }

    let m = date.getMinutes();
    if (m < 10) { m = `0${m}`; }

    return `${d}/${M}/${y} - ${h}:${m}`;
  };

  const sizeToTime = function(size) {
    let time;
    for (let key in timeEstimate) {
      const value = timeEstimate[key];
      if (size < key) {
        time = value;
        break;
      }
    }
    return time;
  };

  return File = Backbone.Model.extend({

    initialize() {
      let action, classes, date, storage;
      const path = this.get('path');
      if (path.startsWith('/dropbox/')) {
        storage = 'dropbox';
      } else if (path.startsWith('/supporting-documents/')) {
        storage = 'metadata';
      } else if (path.startsWith('/eidchub/')) {
        storage = 'datastore';
      }

      const status = this.get('status');
      const errorType = status in errorTypes ? errorTypes[status] : 'valid';
      const open = validTypes.has(status) ? false : true;

      if (open && (errorType !== 'valid')) {
        classes = 'panel-danger';
      } else if (open) {
        classes = 'panel-default';
      } else if (errorType !== 'valid') {
        classes = 'is-collapsed';
      } else {
        classes = 'panel-default is-collapsed';
      }

      if (errorType === 'valid') {
        action = keyToAction[storage];
      } else {
        action = errorToAction[status];
      }

      const size = this.has('bytes') ? filesize(this.get('bytes')) : 0;
      if (this.has('time')) { date = simpleDate(this.get('time')); }
      const hash = this.has('hash') ? this.get('hash') : 'NO_HASH';
      const estimate = sizeToTime(this.get('bytes'));
      const message = messages[status];
      const moving = status.includes('MOVING') || (status === 'WRITING');
      const validating = status === 'VALIDATING_HASH';

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
      });
    },

    update(data) {
      this.set({
        check: false,
        date: simpleDate(data.time),
        estimate: sizeToTime(data.bytes),
        hash: data.hash,
        message: messages[status],
        moving: false,
        size: filesize(data.bytes),
        status: data.status
      });
      return this.initialize();
    },

    copy(path) {
      return new File({
        bytes: this.get('bytes'),
        check: true,
        name: this.get('name'),
        path: this.get('path').replace(/^\/(dropbox|eidchub|supporting-documents)\//, path),
        status: 'MOVING_TO'
      });
    }
  });
});