package app.release.model;

import org.kohsuke.args4j.Option;

public class CommandLineArguments {

    @Option(name = "-key", required = true, usage = "JSON key file of authorized service account")
    private String jsonKeyPath;

    @Option(name = "-name", usage = "(optional) Provide with AAB File")
    private String appName;

    @Option(name = "-packageName", usage = "(optional) Provide with AAB File")
    private String packageName;

    @Option(name = "-file", required = true, usage = "APK Or AAB file to be released")
    private String file;

    @Option(name = "-track", required = true, usage = "Release track to use. Eg. internal, alpha, beta or production")
    private String trackName;

    @Option(name = "-notes", forbids = "-notesFile", usage = "(optional) Release notes")
    private String notes;

    @Option(name = "-notesFile", forbids = "-notes", usage = "(optional) Release notes from file")
    private String notesPath;

    public String getJsonKeyPath() {
        return jsonKeyPath;
    }

    public void setJsonKeyPath(String jsonKeyPath) {
        this.jsonKeyPath = jsonKeyPath;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotesPath() {
        return notesPath;
    }

    public void setNotesPath(String notesPath) {
        this.notesPath = notesPath;
    }

    public String getPackageName() {
        return packageName;
    }
}
