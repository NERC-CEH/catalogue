import $ from 'jquery'
import { SimpleUploadView } from './App'

$(document).ready(function () {
  const url = $('#simple-upload-dropzone').attr('action')
  const view = new SimpleUploadView({
    el: '#simple-upload',
    url
  })
  view.render()
})
