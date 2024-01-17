import $ from 'jquery'
import { DropzoneView } from '../../src/Upload'

describe('DropzoneView', function () {
    let view

    beforeEach(function () {
        const dropzoneSuccess = jasmine.createSpy('dropzoneSuccess')
        $(document.body).html(`
                <div class ="dropzone-container">
                        <div class="dropzone-files"></div>
                        <div class="fileinput-button"></div>
                </div>
        `)

        view = new DropzoneView({
            el: '.dropzone-container',
            success: dropzoneSuccess,
            url: '/file/post'
        })
    })

    it('is defined and dropzoneOptions works', function () {
        // then
        expect(view).toBeDefined()
    })
})
