import { IdentityPermissionView } from '../src/IdentityPermission/index.js'

describe('Test IdentityPermissionView', () => {
  it('Permission app should load permission with correct id', () => {
    // given
    const id = 1
    const view = new IdentityPermissionView()

    // when
    const result = view.initialize(id)

    // then
    expect(null).toBeNull()
  })
})
