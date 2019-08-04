package app.release.publisher.android;

import app.release.model.CommandLineArguments;
import app.release.publisher.Publisher;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.*;

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
 * Uploads android aab files to Play Store.
 */
public class AabPublisher implements Publisher {

    private static final String MIME_TYPE_AAB = "application/octet-stream";
    private CommandLineArguments arguments;


    public AabPublisher(CommandLineArguments arguments) {
        this.arguments = arguments;
    }

    /**
     * Perform aab publish an release on given track
     *
     * @throws Exception Upload error
     */
    @Override
    public void publish() throws IOException {

        // load key file credentials
        System.out.println("Loading account credentials...");
        Path jsonKey = FileSystems.getDefault().getPath(arguments.getJsonKeyPath()).normalize();
        GoogleCredential cred = GoogleCredential.fromStream(new FileInputStream(jsonKey.toFile()));
        cred = cred.createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

        // load aab file info
        System.out.println("Loading file information...");
        Path file = FileSystems.getDefault().getPath(arguments.getFile()).normalize();
        String applicationName = arguments.getAppName();
        String packageName = arguments.getPackageName();
        System.out.println("Application Name: " + applicationName);
        System.out.println("Package Name: " + packageName);

        // load release notes
        System.out.println("Loading release notes...");
        List<LocalizedText> releaseNotes = new ArrayList<>();
        if (arguments.getNotesPath() != null) {
            Path notesFile = FileSystems.getDefault().getPath(arguments.getNotesPath()).normalize();
            String notesContent = null;
            try {
                notesContent = new String(Files.readAllBytes(notesFile));
                releaseNotes.add(new LocalizedText().setLanguage(Locale.US.toString()).setText(notesContent));
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (arguments.getNotes() != null) {
            releaseNotes.add(new LocalizedText().setLanguage(Locale.US.toString()).setText(arguments.getNotes()));
        }

        // init publisher
        System.out.println("Initialising publisher service...");
        com.google.api.services.androidpublisher.AndroidPublisher.Builder ab = new com.google.api.services.androidpublisher.AndroidPublisher.Builder(cred.getTransport(), cred.getJsonFactory(), setHttpTimeout(cred));
        com.google.api.services.androidpublisher.AndroidPublisher publisher = ab.setApplicationName(applicationName).build();

        // create an edit
        System.out.println("Initialising new edit...");
        AppEdit edit = null;
        try {
            edit = publisher.edits().insert(packageName, null).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final String editId = edit.getId();
        System.out.println(String.format("Edit created. Id: %s", editId));

        try {
            // publish the file
            System.out.println("Uploading AAB file...");
            AbstractInputStreamContent aabContent = new FileContent(MIME_TYPE_AAB, file.toFile());
            Bundle bundle = publisher.edits().bundles().upload(packageName, editId, aabContent).execute();
            System.out.println(String.format("File uploaded. Version Code: %s", bundle.getVersionCode()));

            // create a release on track
            System.out.println(String.format("On track:%s. Creating a release...", arguments.getTrackName()));
            TrackRelease release = new TrackRelease().setName("Automated publish").setStatus("completed")
                    .setVersionCodes(Collections.singletonList((long) bundle.getVersionCode()))
                    .setReleaseNotes(releaseNotes);
            Track track = new Track().setReleases(Collections.singletonList(release));
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
            e.printStackTrace();
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

    private HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer) {
        return httpRequest -> {
            requestInitializer.initialize(httpRequest);
            httpRequest.setConnectTimeout(3 * 60000);  // 3 minutes connect timeout
            httpRequest.setReadTimeout(3 * 60000);  // 3 minutes read timeout
        };
    }
}