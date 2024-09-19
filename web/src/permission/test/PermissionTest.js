import { Permission } from '../src/PermissionApp'

describe('Test Permission', () => {
  it('Should have doctype in urlRoot', () => {
    // given
    const id = 'myId'
    const doctype = 'myDoctype'
    const model = { doctype, id }
    const permission = new Permission(model)

    // when
    const result = permission.urlRoot()

    // then
    const expected = `/${doctype}/${id}/permission`
    expect(result).toEqual(expected)
  })
})
