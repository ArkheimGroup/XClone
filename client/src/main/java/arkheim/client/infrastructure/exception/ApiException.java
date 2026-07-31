package arkheim.client.infrastructure.exception;

import arkheim.client.domain.dtos.ApiResponse;

public class ApiException extends RuntimeException {
    private final int statusCode;
    private final ApiResponse apiResponse;

    public ApiException(int statusCode, ApiResponse apiResponse) {
        super(apiResponse != null ? apiResponse.getMessage() : "Unknown API error");
        this.statusCode = statusCode;
        this.apiResponse = apiResponse;
    }

    public int getStatusCode() { return statusCode; }
    public ApiResponse getApiResponse() { return apiResponse; }

    /**
     * Helper to check if the error is safe to show to a normal user.
     * If resultCode is null, it indicates an unhandled server/network exception.
     */
    public boolean isUserFriendly() {
        return apiResponse != null && apiResponse.getResultCode() != null;
    }
}

