package app.release.publisher;

import app.release.publisher.android.AabPublisher;
import app.release.publisher.android.ApkPublisher;
import app.release.model.CommandLineArguments;

public class PublisherFactory {

    private PublisherFactory() {
    }

    public static Publisher buildPublisher(CommandLineArguments arguments) {
        String fileName = arguments.getFile();
        Publisher publisher;
        if (fileName.toLowerCase().endsWith(".apk")) {
            publisher = new ApkPublisher(arguments);
        } else if (fileName.toLowerCase().endsWith(".aab")) {
            publisher = new AabPublisher(arguments);
        } else {
            throw new RuntimeException("File Type is not supported for: " + fileName);
        }

        return publisher;
    }
}
