import Swal from 'sweetalert2'
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

    it('renders a conflict-specific dialog and not the generic one on a 409 save error', () => {
      // given
      const { model, view } = createView()
      spyOn(model, 'save').and.callFake((attrs, options) => {
        options.error(model, { status: 409, statusText: 'Conflict' })
      })
      const swalSpy = spyOn(Swal, 'fire')

      // when
      view.save()

      // then
      expect(swalSpy).toHaveBeenCalled()
      const args = swalSpy.calls.mostRecent().args[0]
      expect(args.title).toBe('Edit conflict')
      expect(args.icon).toBe('warning')
      // this must be the conflict-specific dialog, not the generic error dialog
      expect(args.title).not.toContain('Server response')
      expect(swalSpy.calls.count()).toBe(1)
    })

    it('renders a generic error dialog (not the conflict wording) on a non-409 save error', () => {
      // given
      const { model, view } = createView()
      spyOn(model, 'save').and.callFake((attrs, options) => {
        options.error(model, { status: 500, statusText: 'Server Error' })
      })
      const swalSpy = spyOn(Swal, 'fire')

      // when
      view.save()

      // then
      expect(swalSpy).toHaveBeenCalled()
      const args = swalSpy.calls.mostRecent().args[0]
      expect(args.title).toBe('Server response: 500 Server Error')
      expect(args.icon).toBe('error')
      expect(args.title).not.toBe('Edit conflict')
      expect(JSON.stringify(args)).not.toMatch(/changed by another user/)
    })
  })
})
