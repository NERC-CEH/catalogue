/**
 * The one list of file formats the editors offer: the dataset and UKEMS "Formats" presets and the
 * service agreement format picker. Each entry has a display name, a media type (empty where none is
 * registered) and the common file extensions, without the dot.
 */
export const FORMATS = [
  { name: 'Comma-separated values (CSV)', type: 'text/csv', extensions: ['csv'] },
  { name: 'NetCDF', type: 'application/netcdf', extensions: ['nc'] },
  { name: 'Parquet', type: 'application/vnd.apache.parquet', extensions: ['parquet'] },
  { name: 'Shapefile', type: '', extensions: ['shp'] },
  { name: 'TIFF', type: 'image/tiff', extensions: ['tif', 'tiff'] }
]

export default FORMATS

/** Lower-cases, trims and drops one leading dot, so `' .CSV '` matches `csv`. */
function normalise (text) {
  return String(text ?? '').trim().replace(/^\./, '').toLowerCase()
}

/** The entry whose name or extension matches `text`, ignoring case, spaces and a leading dot. */
export function findFormat (text) {
  const wanted = normalise(text)
  if (!wanted) return undefined
  return FORMATS.find(f => normalise(f.name) === wanted || f.extensions.some(e => e === wanted))
}

/** An object keyed by entry name, in list order, whose values come from `toAttributes(entry)`. */
export function predefinedFormats (toAttributes) {
  return Object.fromEntries(FORMATS.map(f => [f.name, toAttributes(f)]))
}
