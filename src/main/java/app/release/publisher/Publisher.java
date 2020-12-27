package app.release.publisher;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface Publisher {

    void publish() throws IOException, GeneralSecurityException;

}
