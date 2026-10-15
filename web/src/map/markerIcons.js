/*
 * Leaflet's default marker, from the bundled images. Leaflet finds its images from the URL in its
 * CSS, expecting it to end in marker-icon.png; bundled, it ends marker-icon.<hash>.png, so Leaflet
 * fell back to a bare marker-icon.png, fetched beside the page (/documents/marker-icon.png, a 404) and
 * drew every point as a broken image. Imported for its effect by each view that draws points.
 */
import L from 'leaflet'
import iconRetinaUrl from 'leaflet/dist/images/marker-icon-2x.png'
import iconUrl from 'leaflet/dist/images/marker-icon.png'
import shadowUrl from 'leaflet/dist/images/marker-shadow.png'

// No path to put before the URLs below, which are whole: an empty one also stops Leaflet looking
L.Icon.Default.imagePath = ''
L.Icon.Default.mergeOptions({ iconRetinaUrl, iconUrl, shadowUrl })
