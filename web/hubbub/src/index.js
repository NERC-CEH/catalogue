import $ from 'jquery'
import 'bootstrap'
import { UploadView, UploadModel } from './Upload'

$(document).ready(function () {
  //  TODO: load state of the app from html, state set from Jira state
  const id = $('#document-upload').data('guid')
  const model = new UploadModel()
  model.set('id', id)
  return new UploadView({
    el: '#document-upload',
    model
  })
})
