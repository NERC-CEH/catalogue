import { EditorMetadata } from '../src'

describe('EditorMetadata', () => {
    describe('validation', () => {
        it('success with title', () => {
            const editor = new EditorMetadata({
                title: 'this is a title'
            })
            const errors = editor.validate(editor.attributes)
            expect(errors).not.toBeDefined()
        })

        it('produces error without title', () => {
            const editor = new EditorMetadata()
            const errors = editor.validate(editor.attributes)
            expect(errors).toHaveSize(1)
        })
    })
})
