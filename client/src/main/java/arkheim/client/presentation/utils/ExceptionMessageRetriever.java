package arkheim.client.presentation.utils;

import arkheim.client.infrastructure.exception.ApiException;

public class ExceptionMessageRetriever {
    public static String getMessage(Exception e) {
        if (e instanceof ApiException exception) {
            if (exception.isUserFriendly()) {
                return exception.getMessage();
            }
        }

        return "An error has occurred";
    }
}
