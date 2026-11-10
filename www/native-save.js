// Saving on Android (Capacitor).
//
// Android's WebView can't save <a download> blob links, so inside the Android app
// we hand the image to our own small native plugin (GallerySaverPlugin.java), which
// saves it straight into the gallery in Pictures/Resizer.
//
// In a normal browser or in Electron, nativeSave() returns false and the page uses
// its usual download code instead.
(function () {
  const cap = window.Capacitor;
  const isNative = !!(cap && cap.isNativePlatform && cap.isNativePlatform());
  const GallerySaver = isNative && cap.registerPlugin ? cap.registerPlugin('GallerySaver') : null;

  // Blob -> base64 text (native plugins receive data as text, not as a Blob)
  function blobToBase64(blob) {
    return new Promise((resolve, reject) => {
      const r = new FileReader();
      r.onload = () => resolve(String(r.result).split(',')[1]);
      r.onerror = () => reject(r.error);
      r.readAsDataURL(blob);
    });
  }

  // Returns a message for the status line, or false if not running in the Android app.
  window.nativeSave = async function (filename, blob) {
    if (!GallerySaver) return false;
    const data = await blobToBase64(blob);
    await GallerySaver.save({ data, fileName: filename, mimeType: blob.type || 'image/png' });
    return 'Saved to Gallery (Pictures/Resizer): ' + filename;
  };
})();
