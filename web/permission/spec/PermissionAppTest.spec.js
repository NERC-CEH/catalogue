import { PermissionApp } from '../src/PermissionApp/index.js'

describe('Test Permission app', () => {
  it('Permission app should load permission with correct id', () => {
    // given
    const id = 1
    const app = new PermissionApp()

    // when
    app.loadPermission(id)

    // then
    expect(app.getPermission().urlRoot()).toBe(`/documents/${id}/permission`)
  })
})
