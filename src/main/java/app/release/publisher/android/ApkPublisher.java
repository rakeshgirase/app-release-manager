package app.release.publisher.android;

import app.release.model.CommandLineArguments;
import app.release.publisher.Publisher;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.*;
import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Uploads android apk files to Play Store.
 */
public class ApkPublisher implements Publisher {

    private static final String MIME_TYPE_APK = "application/vnd.android.package-archive";
    private CommandLineArguments arguments;

    public ApkPublisher(CommandLineArguments arguments) {
        this.arguments = arguments;
    }

    @Override
    public void publish() throws IOException {

        // load key file credentials
        System.out.println("Loading account credentials...");
        Path jsonKey = FileSystems.getDefault().getPath(arguments.getJsonKeyPath()).normalize();
        GoogleCredential cred = GoogleCredential.fromStream(new FileInputStream(jsonKey.toFile()));
        cred = cred.createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

        // load apk file info
        System.out.println("Loading apk file information...");
        Path apkFile = FileSystems.getDefault().getPath(arguments.getFile()).normalize();
        ApkFile apkInfo = new ApkFile(apkFile.toFile());
        ApkMeta apkMeta = apkInfo.getApkMeta();
        final String applicationName = arguments.getAppName() == null ? apkMeta.getName() : arguments.getAppName();
        final String packageName = apkMeta.getPackageName();
        System.out.println(String.format("ApplicationPublisher Name: %s", apkMeta.getName()));
        System.out.println(String.format("ApplicationPublisher Id: %s", apkMeta.getPackageName()));
        System.out.println(String.format("ApplicationPublisher Version Code: %d", apkMeta.getVersionCode()));
        System.out.println(String.format("ApplicationPublisher Version Name: %s", apkMeta.getVersionName()));
        apkInfo.close();

        // load release notes
        System.out.println("Loading release notes...");
        List<LocalizedText> releaseNotes = new ArrayList<>();
        if (arguments.getNotesPath() != null) {
            Path notesFile = FileSystems.getDefault().getPath(arguments.getNotesPath()).normalize();
            String notesContent = new String(Files.readAllBytes(notesFile));
            releaseNotes.add(new LocalizedText().setLanguage(Locale.US.toString()).setText(notesContent));
        } else if (arguments.getNotes() != null) {
            releaseNotes.add(new LocalizedText().setLanguage(Locale.US.toString()).setText(arguments.getNotes()));
        }

        // init publisher
        System.out.println("Initialising publisher service...");
        com.google.api.services.androidpublisher.AndroidPublisher.Builder ab = new com.google.api.services.androidpublisher.AndroidPublisher.Builder(cred.getTransport(), cred.getJsonFactory(), cred);
        com.google.api.services.androidpublisher.AndroidPublisher publisher = ab.setApplicationName(applicationName).build();

        // create an edit
        System.out.println("Initialising new edit...");
        AppEdit edit = publisher.edits().insert(packageName, null).execute();
        final String editId = edit.getId();
        System.out.println(String.format("Edit created. Id: %s", editId));

        try {
            // publish the apk
            System.out.println("Uploading apk file...");
            AbstractInputStreamContent apkContent = new FileContent(MIME_TYPE_APK, apkFile.toFile());
            Apk apk = publisher.edits().apks().upload(packageName, editId, apkContent).execute();
            System.out.println(String.format("Apk uploaded. Version Code: %s", apk.getVersionCode()));

            // create a release on track
            System.out.println(String.format("On track:%s. Creating a release...", arguments.getTrackName()));
            TrackRelease release = new TrackRelease().setName("Automated publish").setStatus("completed")
                    .setVersionCodes(Collections.singletonList((long) apk.getVersionCode()))
                    .setReleaseNotes(releaseNotes);
            Track track = new Track().setReleases(Collections.singletonList(release)).setTrack(arguments.getTrackName());
            publisher.edits().tracks().update(packageName, editId, arguments.getTrackName(), track).execute();
            System.out.println(String.format("Release created on track: %s", arguments.getTrackName()));

            // commit edit
            System.out.println("Committing edit...");
            publisher.edits().commit(packageName, editId).execute();
            System.out.println(String.format("Success. Committed Edit id: %s", editId));

            // Success
        } catch (Exception e) {
            // error message
            String msg = "Operation Failed: " + e.getMessage();

            // abort
            System.err.println("Operation failed due to an error!, Deleting edit...");
            try {
                publisher.edits().delete(packageName, editId).execute();
            } catch (Exception e2) {
                // log abort error as well
                msg += "\nFailed to delete edit: " + e2.getMessage();
            }

            // forward error with message
            throw new IOException(msg, e);
        }
    }
}