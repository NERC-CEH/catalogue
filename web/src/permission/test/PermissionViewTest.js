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

  describe('save error handling', () => {
    function createView () {
      const model = new Permission({ id: 1, doctype: 'documents' })
      const view = new PermissionView({ model })
      view.template = template
      view.initialize()
      view.delegateEvents()
      return { model, view }
    }

    it('triggers a conflict-specific message and not the generic one on a 409 save error', () => {
      // given
      const { model, view } = createView()
      spyOn(model, 'save').and.callFake((attrs, options) => {
        options.error(model, { status: 409, statusText: 'Conflict' })
      })
      spyOn(model, 'trigger').and.callThrough()

      // when
      view.save()

      // then
      const saveErrorCalls = model.trigger.calls.allArgs().filter(args => args[0] === 'save:error')
      expect(saveErrorCalls.length).toBe(1)
      expect(saveErrorCalls[0][1]).toMatch(/changed by another user/)
      expect(saveErrorCalls[0][1]).not.toMatch(/^Error saving permission:/)
    })

    it('still triggers the generic message on a non-409 save error', () => {
      // given
      const { model, view } = createView()
      spyOn(model, 'save').and.callFake((attrs, options) => {
        options.error(model, { status: 500, statusText: 'Server Error' })
      })
      spyOn(model, 'trigger').and.callThrough()

      // when
      view.save()

      // then
      const saveErrorCalls = model.trigger.calls.allArgs().filter(args => args[0] === 'save:error')
      expect(saveErrorCalls.length).toBe(1)
      expect(saveErrorCalls[0][1]).toBe('Error saving permission: 500 (Server Error)')
      expect(saveErrorCalls[0][1]).not.toMatch(/changed by another user/)
    })
  })
})
