package libman.util;

import java.util.UUID;

public final class SubmissionTokenUtil8 {
    private SubmissionTokenUtil8() {
    }

    public static String create() {
        return UUID.randomUUID().toString();
    }
}
