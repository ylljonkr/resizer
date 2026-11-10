// Saving on Android (Capacitor).
//
// Android's WebView can't save <a download> blob links, so inside the Android app
// we write the image to the app's cache folder with the Filesystem plugin and then
// open the Share sheet. From there you pick Photos, Files, Drive, etc.
//
// In a normal browser or in Electron, nativeSave() returns false and the page uses
// its usual download code instead.
(function () {
  const cap = window.Capacitor;
  const isNative = !!(cap && cap.isNativePlatform && cap.isNativePlatform());

  let Filesystem = null;
  let Share = null;
  if (isNative && cap.registerPlugin) {
    Filesystem = cap.registerPlugin('Filesystem');
    Share = cap.registerPlugin('Share');
  }

  // Blob -> base64 text (the Filesystem plugin takes base64 for binary files)
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
    if (!Filesystem || !Share) return false;

    const data = await blobToBase64(blob);
    const written = await Filesystem.writeFile({
      path: 'exports/' + filename,
      data,
      directory: 'CACHE',
      recursive: true,
    });

    try {
      await Share.share({ title: filename, files: [written.uri], dialogTitle: 'Save or share image' });
      return 'Shared ' + filename;
    } catch (e) {
      // Closing the Share sheet without choosing an app counts as "cancel", not an error.
      if (e && /cancel/i.test(String(e.message || e))) return 'Save cancelled.';
      throw e;
    }
  };
})();
