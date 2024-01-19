import { IdentityPermissionView } from '../src/IdentityPermission/index.js'
import { Permission } from '../src/PermissionApp/index.js'

describe('Test IdentityPermissionView', () => {
    it('View should be defined', () => {
        // when
        const view = new IdentityPermissionView()

        // then
        expect(view).toBeDefined()
    })

    it('Permissions should be removed', () => {
        // given
        const model = new Permission({
            identity: 1,
            canView: true,
            canEdit: true,
            canDelete: true,
            canUpload: false
        })
        const view = new IdentityPermissionView({ model })
        spyOn(view, 'removePermission')
        view.initialize()
        view.delegateEvents()
        view.render()

        // when
        view.$('button').trigger('click')

        // then
        expect(view.removePermission).toHaveBeenCalled()
    })

    it('Update should be called', () => {
        // given
        const model = new Permission({
            identity: 1,
            canView: true,
            canEdit: true,
            canDelete: true,
            canUpload: false
        })
        const view = new IdentityPermissionView({ model })
        spyOn(view, 'update')
        view.initialize()
        view.delegateEvents()
        view.render()

        // when
        view.$('[type="checkbox"]').trigger('click')

        // then
        expect(view.update).toHaveBeenCalled()
    })
})
