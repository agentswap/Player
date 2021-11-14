package org.easyrpg.player.player;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A wrapper around SAF for use from JNI
 */
public class SafFile {
    private Context context = null;
    private DocumentFile root = null;

    private SafFile(Context context, DocumentFile root) {
        this.context = context;
        this.root = root;
    }

    public static SafFile fromPath(Context context, String path) {
        // Attempt to properly URL encode the path

        // Example:
        // The Player will pass in here as the game directory:
        // primary%3Aeasyrpg/document/primary%3Aeasyrpg%2Fgames%2FTestGame

        // When a file inside the game is accessed the path will be:
        // primary%3Aeasyrpg/document/primary%3Aeasyrpg%2Fgames%2FTestGame/Title/Title.png
        // The /Title/Title.png must be URL encoded, otherwise DocumentFile rejects it

        int encoded_slash_pos = path.lastIndexOf("%2F");
        if (encoded_slash_pos == -1) {
            // Should not happen because the game is in a subdirectory
            Log.e("EasyRPG", "Strange Uri " + path);
            return null;
        }
        int slash_pos = path.indexOf("/", encoded_slash_pos);

        // A file is provided when a / is after the encoded / (%2F)
        if (slash_pos > -1) {
            // Extract the filename and properly encode it
            String encoded = path.substring(0, slash_pos);
            String to_encode = path.substring(slash_pos);
            to_encode = Uri.encode(to_encode);
            path = encoded + to_encode;
        }

        Uri uri = Uri.parse("content://" + path);

        DocumentFile f = DocumentFile.fromTreeUri(context, uri);
        return new SafFile(context, f);
    }

    public boolean isFile() {
        return root != null && root.isFile();
    }

    public boolean isDirectory() {
        return root != null && root.isDirectory();
    }

    public boolean exists() {
        return root != null && root.exists();
    }

    public long getFilesize() {
        if (root == null || !root.exists()) {
            return -1L;
        }

        return root.length();
    }

    public int createInputFileDescriptor() {
        if (root == null) {
            return -1;
        }

        // No difference between read mode and binary read mode
        try (ParcelFileDescriptor fd = context.getContentResolver().openFile(root.getUri(), "r", null)) {
            return fd.detachFd();
        } catch (IOException e) {
            return -1;
        }
    }

    public int createOutputFileDescriptor(boolean append) {
        if (root == null) {
            return -1;
        }

        String mode = "w";
        if (append) {
            mode += "a";
        }

        Uri actualFile = root.getUri();
        if (!exists()) {
            // The file must exist beforehand
            // To create it the parent directory must be obtained
            String full_path = root.getUri().toString();
            String directory = full_path.substring(0, full_path.lastIndexOf("%2F"));
            String filename = full_path.substring(full_path.lastIndexOf("%2F") + 3);
            filename = Uri.decode(filename);
            DocumentFile df = DocumentFile.fromTreeUri(context, Uri.parse(directory));
            if (df == null || !df.exists()) {
                return -1;
            }
            df = df.createFile("application/octet-stream", filename);
            if (df == null || !df.exists()) {
                return -1;
            }
            // createFile can decide to not honor the filename, wtf?!
            actualFile = df.getUri();
        }

        try (ParcelFileDescriptor fd = context.getContentResolver().openFile(actualFile, mode, null)) {
            return fd.getFd();
        } catch (IOException e) {
            return -1;
        }
    }

    DirectoryTree getDirectoryContent() {
        if (root == null || !root.isDirectory()) {
            return null;
        }

        ArrayList<String> files = new ArrayList<>();
        ArrayList<Boolean> is_dir = new ArrayList<>();

        for (DocumentFile file: root.listFiles()) {
            files.add(file.getName());
            is_dir.add(file.isDirectory());
        }

        return new DirectoryTree(files, is_dir);
    }
}
