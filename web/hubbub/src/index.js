import $ from 'jquery'
import Bootstrap from 'bootstrap'
import HubbubUploadView from './UploadView'
import HubbubUploadModel from './Upload'

$(document).ready(function () {
  //  TODO: load state of the app from html, state set from Jira state
  const id = $('#document-upload').data('guid')
  const model = new HubbubUploadModel()
  model.set('id', id)
  return new HubbubUploadView({
    el: '#document-upload',
    model
  })
})
