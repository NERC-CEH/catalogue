// Whether a field holds a web address (http, https or ftp, or protocol-relative), with a domain that
// may have non-ASCII letters. One copy for the editor's models: there were four, two of whose \u
// escapes had lost their `u` and so refused such domains (dri-one #473).
// From https://github.com/jzaefferer/jquery-validation/blob/043fb91da7dab0371fded5e2a9fa7ebe4c836210/src/core.js#L1180
const WEB_ADDRESS = /^(?:(?:https?|ftp):)?\/\/(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4])|(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*\.[a-z\u00a1-\uffff]{2,}.?)(?::\d{2,5})?(?:[/?#]\S*)?$/

export function isValidUrl (url) {
  return WEB_ADDRESS.test(url)
}
