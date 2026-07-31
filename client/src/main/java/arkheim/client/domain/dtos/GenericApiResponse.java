package arkheim.client.domain.dtos;

public class GenericApiResponse<T> extends ApiResponse {
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
