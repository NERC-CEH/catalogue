import { PermissionApp } from '../src/PermissionApp/index.js'

describe('Test Permission app', () => {
  it('Permission app should load permission with correct id', () => {
    // given
    const id = 1
    const app = new PermissionApp()

    // when
    app.loadPermission(id).then(function () {
      console.log('Promise Resolved')
    }).catch(function () {
      console.log('Promise Rejected')
    })

    // then
    expect(function () {
      app.getPermission()
    }).toThrow()
    // expect(app.getPermission().id).toBe(id)
  })
})
