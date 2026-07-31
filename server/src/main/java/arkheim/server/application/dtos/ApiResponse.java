package arkheim.server.application.dtos;

import arkheim.server.domain.exception.ResultCode;

public class ApiResponse {
    private boolean success;
    private ResultCode resultCode;
    private String message;

    public void setSuccess(boolean success) { this.success = success; }
    public void setResultCode(ResultCode resultCode) { this.resultCode = resultCode; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return this.success; }
    public ResultCode getResultCode() {return this.resultCode; }
    public String getMessage() { return this.message; }

    public static ApiResponse success(ResultCode resultCode, String message) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(true);
        response.setResultCode(resultCode);
        response.setMessage(message);
        return response;
    }

    public static ApiResponse success(ResultCode resultCode) {
        return success(resultCode, resultCode.name());
    }

    public static ApiResponse failure(ResultCode resultCode, String message) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(false);
        response.setResultCode(resultCode);
        response.setMessage(message);
        return response;
    }

    public static ApiResponse failure(ResultCode resultCode) {
        return failure(resultCode, resultCode.name());
    }
}
