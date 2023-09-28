import { Permission, PermissionView } from '../src/PermissionApp'
import template from '../src/PermissionApp/PermissionsTemplate'

describe('Test PermissionView', () => {
  it('View should be defined', () => {
    // when
    const view = new PermissionView()

    // then
    expect(view).toBeDefined()
  })

  it('Save should be triggered', () => {
    // given
    const model = new Permission({
      id: 1,
      identity: 1,
      canView: true,
      canEdit: true,
      canDelete: true,
      canUpload: false
    })
    const view = new PermissionView({ model })
    view.template = template
    spyOn(view, 'reload')
    view.initialize()
    view.delegateEvents()

    // when
    model.trigger('sync')

    // then
    expect(view.reload).toHaveBeenCalled()
  })

  it('addAll should be triggered when permission is added', () => {
    // given
    const model = new Permission({
      id: 1,
      identity: 1,
      canView: true,
      canEdit: true,
      canDelete: true,
      canUpload: false
    })
    model.loadCollection()
    const view = new PermissionView({ model })
    view.template = template
    spyOn(view, 'addAll')
    view.initialize()
    view.delegateEvents()

    // when
    model.trigger('permission:add')

    // then
    expect(view.addAll).toHaveBeenCalled()
  })

  it('addAll should be triggered when permission is removed', () => {
    // given
    const model = new Permission({
      id: 1,
      identity: 1,
      canView: true,
      canEdit: true,
      canDelete: true,
      canUpload: false
    })
    model.loadCollection()
    const view = new PermissionView({ model })
    view.template = template
    spyOn(view, 'addAll')
    view.initialize()
    view.delegateEvents()

    // when
    model.trigger('permission:remove')

    // then
    expect(view.addAll).toHaveBeenCalled()
  })
})
