package arkheim.server.application.dtos;

import arkheim.server.domain.exception.ResultCode;

public class GenericApiResponse<T> extends ApiResponse {
    private T data;

    public void setData(T data) { this.data = data; }

    public T getData() { return this.data; }

    public static <T> GenericApiResponse<T> success(ResultCode resultCode, String message, T data) {
        GenericApiResponse<T> response = new GenericApiResponse<>();
        response.setSuccess(true);
        response.setResultCode(resultCode);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> GenericApiResponse<T> success(ResultCode resultCode, T data) {
        return success(resultCode, resultCode.name(), data);
    }

    public static <T> GenericApiResponse<T> failure(ResultCode resultCode, String message, T data) {
        GenericApiResponse<T> response = new GenericApiResponse<>();
        response.setSuccess(false);
        response.setResultCode(resultCode);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> GenericApiResponse<T> failure(ResultCode resultCode, T data) {
        return failure(resultCode, resultCode.name(), data);
    }
}
