const UNITS = ['B', 'KB', 'MB', 'GB', 'TB']

/**
 * A file size for people to read: "384 KB", "1.5 MB". Binary multiples (a kilobyte is 1024 bytes)
 * with the familiar unit names, to at most two decimal places. The pages showed sizes two ways,
 * through the filesize package, and the hubbub table's size sort assumed this one (dri-one #532).
 */
export const formatBytes = bytes => {
  if (!Number.isFinite(bytes) || bytes <= 0) return '0 B'
  let unit = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), UNITS.length - 1)
  let value = Number((bytes / 1024 ** unit).toFixed(2))
  // 1023.999 KB rounds to 1024 KB: that is 1 MB
  if (value >= 1024 && unit < UNITS.length - 1) {
    unit += 1
    value = Number((bytes / 1024 ** unit).toFixed(2))
  }
  return `${value} ${UNITS[unit]}`
}
