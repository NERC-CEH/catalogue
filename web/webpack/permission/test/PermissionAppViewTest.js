import { Permission, PermissionApp, PermissionAppView } from '../src/PermissionApp'
import template from '../src/PermissionApp/Permissions.tpl'

describe('Test PermissionAppView', () => {
  it('View should be defined', () => {
    // when
    const view = new PermissionAppView()

    // then
    expect(view).toBeDefined()
  })

  it('Render should be triggered', () => {
    // given
    const permission = new Permission({
      id: 1,
      identity: 1,
      canView: true,
      canEdit: true,
      canDelete: true,
      canUpload: false
    })
    permission.loadCollection()
    const model = new PermissionApp({
      permission: permission
    })
    const view = new PermissionAppView({ model: model })
    view.template = template
    spyOn(view, 'render')
    view.initialize()
    view.delegateEvents()

    // when
    model.trigger('loaded')

    // then
    expect(view.render).toHaveBeenCalled()
  })
})
