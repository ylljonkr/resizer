package com.ylljonkr.resizer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Saves an image into the phone's gallery, in Pictures/Resizer.
 *
 * Android 10+ : goes through MediaStore (Android's photo catalog). No permission needed.
 * Android 7-9 : writes the file directly and asks the media scanner to index it.
 *               Needs the storage permission, which Android asks for on first save.
 *
 * Called from www/native-save.js as GallerySaver.save({ data, fileName, mimeType }).
 */
@CapacitorPlugin(
    name = "GallerySaver",
    permissions = { @Permission(alias = "storage", strings = { Manifest.permission.WRITE_EXTERNAL_STORAGE }) }
)
public class GallerySaverPlugin extends Plugin {

    private static final String ALBUM = "Resizer";

    @PluginMethod
    public void save(PluginCall call) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && getPermissionState("storage") != PermissionState.GRANTED) {
            requestPermissionForAlias("storage", call, "storagePermissionDone");
            return;
        }
        doSave(call);
    }

    @PermissionCallback
    private void storagePermissionDone(PluginCall call) {
        if (getPermissionState("storage") == PermissionState.GRANTED) {
            doSave(call);
        } else {
            call.reject("Storage permission was denied.");
        }
    }

    private void doSave(PluginCall call) {
        String data = call.getString("data");
        String fileName = call.getString("fileName");
        String mimeType = call.getString("mimeType", "image/png");
        if (data == null || fileName == null) {
            call.reject("data and fileName are required.");
            return;
        }

        byte[] bytes;
        try {
            bytes = Base64.decode(data, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            call.reject("The image data was not valid base64.");
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveWithMediaStore(bytes, fileName, mimeType);
            } else {
                saveLegacy(bytes, fileName, mimeType);
            }
            call.resolve();
        } catch (IOException e) {
            call.reject("Could not save to gallery: " + e.getMessage());
        }
    }

    // Android 10 and newer
    private void saveWithMediaStore(byte[] bytes, String fileName, String mimeType) throws IOException {
        ContentResolver resolver = getContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + ALBUM);
        values.put(MediaStore.Images.Media.IS_PENDING, 1); // hide it until it's fully written

        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) throw new IOException("the gallery refused the new image");

        try (OutputStream out = resolver.openOutputStream(uri)) {
            if (out == null) throw new IOException("could not open the new image for writing");
            out.write(bytes);
        } catch (IOException e) {
            resolver.delete(uri, null, null); // don't leave a broken, empty entry behind
            throw e;
        }

        values.clear();
        values.put(MediaStore.Images.Media.IS_PENDING, 0); // now show it in the gallery
        resolver.update(uri, values, null, null);
    }

    // Android 7 to 9
    private void saveLegacy(byte[] bytes, String fileName, String mimeType) throws IOException {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALBUM);
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("could not create Pictures/" + ALBUM);

        File file = new File(dir, fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(bytes);
        }
        MediaScannerConnection.scanFile(getContext(), new String[] { file.getAbsolutePath() }, new String[] { mimeType }, null);
    }
}
