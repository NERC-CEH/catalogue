import $ from 'jquery'
import 'bootstrap'
import { ClipboardCopyView } from './ClipboardCopy'

$(document).ready(function () {
  const view = new ClipboardCopyView({ el: '.clipboard-copy' })
})
