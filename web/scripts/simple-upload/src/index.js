import $ from 'jquery'
import { SimpleUploadView } from './App'

$(document).ready(function () {
  const url = $('#simple-upload-dropzone').attr('action')
  // eslint-disable-next-line no-unused-vars
  const view = new SimpleUploadView({
    el: '#simple-upload',
    url
  })
})
