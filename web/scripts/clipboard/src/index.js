import $ from 'jquery'
import 'bootstrap'
import { ClipboardCopyView } from './ClipboardCopy'

$(document).ready(function () {
  // eslint-disable-next-line no-unused-vars
  const view = new ClipboardCopyView({ el: '.clipboard-copy' })
})
