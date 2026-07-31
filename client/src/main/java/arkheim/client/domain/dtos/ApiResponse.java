package arkheim.client.domain.dtos;

import arkheim.client.infrastructure.exception.ResultCode;

public class ApiResponse {
    private boolean success;
    private ResultCode resultCode;
    private String message;

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public ResultCode getResultCode() { return resultCode; }

    public void setMessage(String message) { this.message = message; }
    public void setResultCode(ResultCode resultCode) { this.resultCode = resultCode; }
}

