import { describe, expect, it } from 'vitest'
import { ABSENT_STATUSES, matchExpected, matchesPattern, patternToRegExp } from '../../src/Expected'

const up = (path, status = 'VALID') => ({ path, status })
const run = (expected, uploads, complete = true) =>
  matchExpected({ files: [], fileGroups: [], ...expected }, uploads, { complete })

describe('patternToRegExp', function () {
  it('matches exactly and case-sensitively', function () {
    expect(patternToRegExp('site1/a.csv').test('site1/a.csv')).toBe(true)
    expect(patternToRegExp('Site1/a.csv').test('site1/a.csv')).toBe(false)
  })

  it('treats folders as part of the match', function () {
    expect(patternToRegExp('a.csv').test('x/a.csv')).toBe(false)
  })

  it('does not let * cross a slash', function () {
    const re = patternToRegExp('site*/a.csv')
    expect(re.test('site12/a.csv')).toBe(true)
    expect(re.test('site1/x/a.csv')).toBe(false)
  })

  it('matches exactly one non-slash character for ?', function () {
    const re = patternToRegExp('a?.csv')
    expect(re.test('a1.csv')).toBe(true)
    expect(re.test('a.csv')).toBe(false)
    expect(re.test('a12.csv')).toBe(false)
    expect(patternToRegExp('a?b').test('a/b')).toBe(false)
  })

  it('is anchored at both ends', function () {
    const re = patternToRegExp('a*.csv')
    expect(re.test('xa1.csv')).toBe(false)
    expect(re.test('a1.csv.bak')).toBe(false)
    expect(re.test('a1.csv')).toBe(true)
  })

  it('treats regular expression metacharacters literally', function () {
    expect(patternToRegExp('data(1).csv').test('data(1).csv')).toBe(true)
    expect(patternToRegExp('a+b.csv').test('a+b.csv')).toBe(true)
    expect(patternToRegExp('v1.2.csv').test('v1x2.csv')).toBe(false)
    expect(patternToRegExp('[a]|b$^{1}\\').test('[a]|b$^{1}\\')).toBe(true)
  })
})

describe('ABSENT_STATUSES', function () {
  it('holds the four statuses that say a file is gone', function () {
    expect([...ABSENT_STATUSES].sort()).toEqual(
      ['MISSING', 'MOVED_UNKNOWN_MISSING', 'UNKNOWN_MISSING', 'ZIPPED_UNKNOWN_MISSING'])
  })
})

describe('matchExpected', function () {
  describe('puts agreed names as an upload stores them (dri-one #536)', function () {
    it('lower-cases a named file', function () {
      const result = run({ files: [{ name: 'CP_North.csv' }] }, [up('cp_north.csv')])
      expect(result.files[0].status).toBe('uploaded')
      expect(result.files[0].name).toBe('CP_North.csv')
    })

    it('turns spaces into hyphens, folders included', function () {
      const result = run({ files: [{ name: 'Site 1/A File.csv' }] }, [up('site-1/a-file.csv')])
      expect(result.files[0].status).toBe('uploaded')
      expect(result.unmatched).toEqual([])
    })

    it('lower-cases a group pattern', function () {
      const result = run({ fileGroups: [{ pattern: 'CP_*_North.csv', count: 1 }] }, [up('cp_x_north.csv')])
      expect(result.groups[0]).toMatchObject({ pattern: 'CP_*_North.csv', found: 1, flag: null })
    })

    it('puts listed paths the same way, as zip folders and registered files keep their case', function () {
      const result = run({ files: [{ name: 'data_2024/site1.csv' }] }, [up('Data_2024/Site1.csv')])
      expect(result.files[0].status).toBe('uploaded')
      expect(result.unmatched).toEqual([])
    })

    it('matches listed paths with capitals and spaces against a group', function () {
      const result = run({ fileGroups: [{ pattern: 'site-*/cp_*.csv', count: 1 }] }, [up('Site 1/CP_A.csv')])
      expect(result.groups[0]).toMatchObject({ found: 1, flag: null })
    })

    it('shows unmatched paths as listed', function () {
      const result = run({ files: [{ name: 'a.csv' }] }, [up('Data_2024/Other File.CSV')])
      expect(result.unmatched).toEqual(['Data_2024/Other File.CSV'])
    })

    it('counts paths that differ only by case or spaces once, as first listed', function () {
      const result = run({ fileGroups: [{ pattern: '*.csv', count: 1 }] }, [up('My Data.csv'), up('my-data.csv')])
      expect(result.groups[0]).toMatchObject({ found: 1, flag: null })
      expect(run({}, [up('My Data.csv'), up('my-data.csv')]).unmatched).toEqual(['My Data.csv'])
    })

    it('counts a lower-cased name as the agreement\'s, not as a group\'s', function () {
      const result = run({ files: [{ name: 'A.csv' }], fileGroups: [{ pattern: '*.csv', count: 1 }] }, [up('a.csv')])
      expect(result.files[0].status).toBe('uploaded')
      expect(result.groups[0].found).toBe(0)
    })
  })

  it('marks a named file uploaded, or not-uploaded when complete', function () {
    const result = run({ files: [{ name: 'a.csv' }, { name: 'b.csv' }] }, [up('a.csv')])
    expect(result.files.map(f => f.status)).toEqual(['uploaded', 'not-uploaded'])
  })

  it('keeps the file attributes in the result', function () {
    const result = run({ files: [{ name: 'a.csv', format: 'csv' }] }, [up('a.csv')])
    expect(result.files[0]).toEqual({ name: 'a.csv', format: 'csv', status: 'uploaded' })
  })

  it('reports unknown rather than not-uploaded when incomplete', function () {
    const result = run({ files: [{ name: 'a.csv' }, { name: 'b.csv' }] }, [up('a.csv')], false)
    expect(result.files.map(f => f.status)).toEqual(['uploaded', 'unknown'])
  })

  it('lets a named file win over a group', function () {
    const result = run(
      { files: [{ name: 'a.csv' }], fileGroups: [{ pattern: '*.csv', count: '1' }] },
      [up('a.csv'), up('b.csv')])
    expect(result.groups[0].found).toBe(1)
    expect(result.unmatched).toEqual([])
  })

  it('lets the first group win over a later one', function () {
    const result = run(
      { fileGroups: [{ pattern: 'a*', count: '1' }, { pattern: '*.csv', count: '1' }] },
      [up('a.csv'), up('b.csv')])
    expect(result.groups.map(g => g.found)).toEqual([1, 1])
  })

  it('counts a path once when it is in both listings', function () {
    const result = run({ fileGroups: [{ pattern: '*.csv', count: 1 }] }, [up('a.csv'), up('a.csv')])
    expect(result.groups[0].found).toBe(1)
    expect(result.groups[0].flag).toBe(null)
  })

  it('does not count files whose status says they are gone', function () {
    const uploads = [...ABSENT_STATUSES].map((status, i) => up(`g${i}.csv`, status))
    uploads.push(up('a.csv'), up('b.csv', 'MISSING_UNKNOWN'))
    const result = run({ files: [{ name: 'g0.csv' }], fileGroups: [{ pattern: '*.csv', count: 2 }] }, uploads)
    expect(result.files[0].status).toBe('not-uploaded')
    expect(result.groups[0].found).toBe(2)
    expect(result.unmatched).toEqual([])
  })

  it('flags a group as over, short or neither', function () {
    const uploads = [up('a1.csv'), up('a2.csv'), up('b1.txt'), up('b1')]
    const result = run({
      fileGroups: [
        { pattern: 'a*.csv', count: '1' },
        { pattern: 'b*.txt', count: '2' },
        { pattern: 'c*', count: '0' },
        { pattern: 'b*', count: '1' }
      ]
    }, uploads)
    expect(result.groups.map(g => g.flag)).toEqual(['over', 'short', null, null])
  })

  it('suppresses short but not over when incomplete', function () {
    const result = run({
      fileGroups: [{ pattern: 'a*', count: '1' }, { pattern: 'b*', count: '2' }]
    }, [up('a1'), up('a2'), up('b1')], false)
    expect(result.groups.map(g => g.flag)).toEqual(['over', null])
  })

  it('skips incomplete entries without throwing', function () {
    const result = run({
      files: [{ format: 'csv' }, { name: '' }],
      fileGroups: [{ count: '2' }, { pattern: 'a*' }, { pattern: 'b*', count: null }]
    }, [up('a.csv'), up('b.csv'), up('b2.csv')])
    expect(result.files.map(f => f.status)).toEqual(['unknown', 'unknown'])
    expect(result.groups[0].found).toBe(0)
    expect(result.groups[0].flag).toBe(null)
    expect(result.groups[1]).toMatchObject({ found: 1, flag: null })
    expect(result.groups[2]).toMatchObject({ found: 2, flag: null })
    expect(result.unmatched).toEqual([])
  })

  it('skips a listed file without a path', function () {
    const result = run({}, [{ status: 'VALID' }, up('a.csv')])
    expect(result.unmatched).toEqual(['a.csv'])
    expect(result.total.found).toBe(1)
  })

  it('copes with absent file lists and fileNumber', function () {
    const result = matchExpected({}, [up('a.csv')], { complete: true })
    expect(result.files).toEqual([])
    expect(result.groups).toEqual([])
    expect(result.unmatched).toEqual(['a.csv'])
    expect(result.total).toEqual({ expected: null, found: 1, differs: false })
  })

  it('compares the total only with a numeric fileNumber', function () {
    const expected = { files: [{ name: 'a.csv' }], fileNumber: ' 3 ' }
    const result = run(expected, [up('a.csv'), up('z.csv')])
    expect(result.total).toEqual({ expected: 3, found: 2, differs: true })
    expect(run({ ...expected, fileNumber: '1' }, [up('a.csv')]).total.differs).toBe(false)
    expect(run(expected, [up('a.csv')], false).total).toEqual({ expected: 3, found: 1, differs: false })
  })

  it('counts every uploaded data file towards the total, matched or not', function () {
    const uploads = [up('a.csv'), up('z.csv'), up('Z.csv'), up('gone.csv', 'MISSING')]
    const result = run({ files: [{ name: 'a.csv' }], fileNumber: '2' }, uploads)
    expect(result.total).toEqual({ expected: 2, found: 2, differs: false })
  })

  it('gives a null expected total for a non-numeric fileNumber', function () {
    for (const fileNumber of ['about 12', '', '1.5', '-2', undefined]) {
      const result = run({ files: [{ name: 'a.csv' }], fileNumber }, [up('a.csv')])
      expect(result.total).toEqual({ expected: null, found: 1, differs: false })
    }
  })

  it('lists only uploads that match nothing as unmatched', function () {
    const result = run(
      { files: [{ name: 'a.csv' }], fileGroups: [{ pattern: 'b*', count: '1' }] },
      [up('a.csv'), up('b1'), up('c.txt'), up('d.txt'), up('c.txt')])
    expect(result.unmatched).toEqual(['c.txt', 'd.txt'])
  })

  it('puts every upload in unmatched when nothing is expected', function () {
    const result = run({}, [up('a.csv'), up('b.csv')])
    expect(result.unmatched).toEqual(['a.csv', 'b.csv'])
  })
})

describe('matchesPattern', function () {
  it('says whether a file name fits a pattern as the upload page would see it', function () {
    expect(matchesPattern('CP_*_MInv_*Stats_*.csv', 'CP_north_MInv_nitrateStats_2020.csv')).toBe(true)
    expect(matchesPattern('CP_*_Stats.csv', 'CP_north_MInv_Stats_2020.csv')).toBe(false)
  })

  it('compares ignoring case and treating spaces as hyphens, on both sides', function () {
    expect(matchesPattern('Site 1/*.CSV', 'site-1/Readings 2024.csv')).toBe(true)
  })

  it('keeps * and ? within one folder', function () {
    expect(matchesPattern('site?/*.csv', 'site1/a.csv')).toBe(true)
    expect(matchesPattern('site?/*.csv', 'site1/x/a.csv')).toBe(false)
  })

  it('matches nothing without both a pattern and a name', function () {
    expect(matchesPattern('', 'a.csv')).toBe(false)
    expect(matchesPattern('*.csv', '')).toBe(false)
    expect(matchesPattern(undefined, undefined)).toBe(false)
  })
})
