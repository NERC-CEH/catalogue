import $ from 'jquery'
import { HubbubUploadModel, HubbubUploadView } from '../src/Upload'

describe('Test DropzoneView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    const id = $('#document-upload').data('guid')
    model = new HubbubUploadModel()
    model.set('id', id)
    view = new HubbubUploadView({
      model
    })
  })

  it('is defined', function () {
    // then
    expect(view).toBeDefined()
  })
})
