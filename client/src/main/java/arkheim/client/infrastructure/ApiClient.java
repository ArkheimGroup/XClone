package arkheim.client.infrastructure;

import arkheim.client.domain.dtos.ApiResponse;
import arkheim.client.domain.dtos.GenericApiResponse;
import arkheim.client.infrastructure.exception.ApiException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import arkheim.client.infrastructure.config.ClientConfig;

public abstract class ApiClient {
    protected final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    protected final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(60))
            .build();
    protected final String baseUrl = ClientConfig.getBaseUrl();


    /**
     * @param context use domain layer entity names
     * @return Response converted to class with type {@link T}
     * @param <T> same as responseType
     */
    protected <T> T send(HttpRequest request, Class<T> dataType, String context) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            int statusCode = response.statusCode();

            Type responseType = TypeToken.getParameterized(GenericApiResponse.class, dataType).getType();
            GenericApiResponse<T> apiResponse = parseGenericResponse(body, responseType, statusCode);

            validateResponse(statusCode, apiResponse);
            return apiResponse.getData();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(context + " HTTP request interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException(context + " HTTP request failed", e);
        }
    }

    /**
     * @param context use domain layer entity names
     */
    protected ApiResponse send(HttpRequest request, String context) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            int statusCode = response.statusCode();

            ApiResponse apiResponse = parseApiResponse(body, statusCode);
            validateResponse(statusCode, apiResponse);
            return apiResponse;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(context + " HTTP request interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException(context + " HTTP request failed", e);
        }
    }

    protected <T> List<T> sendList(HttpRequest request, Class<T> dataType, String context) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            int statusCode = response.statusCode();

            Type dataListType = TypeToken.getParameterized(List.class, dataType).getType();
            Type responseType = TypeToken.getParameterized(GenericApiResponse.class, dataListType).getType();
            GenericApiResponse<List<T>> apiResponse = parseGenericResponse(body, responseType, statusCode);

            validateResponse(statusCode, apiResponse);
            return apiResponse.getData();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(context + " HTTP request interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException(context + " HTTP request failed", e);
        }
    }

    private <T> GenericApiResponse<T> parseGenericResponse(String body, Type responseType, int statusCode) {
        try {
            return gson.fromJson(body, responseType);
        } catch (Exception ex) {
            throw new ApiException(statusCode, buildFallbackResponse());
        }
    }

    private ApiResponse parseApiResponse(String body, int statusCode) {
        try {
            return gson.fromJson(body, ApiResponse.class);
        } catch (Exception ex) {
            throw new ApiException(statusCode, buildFallbackResponse());
        }
    }

    private void validateResponse(int statusCode, ApiResponse response) {
        if (response == null || statusCode < 200 || statusCode >= 300 || !response.isSuccess()) {
            throw new ApiException(statusCode, response != null ? response : buildFallbackResponse());
        }
    }

    private ApiResponse buildFallbackResponse() {
        ApiResponse fallback = new ApiResponse();
        fallback.setSuccess(false);
        fallback.setMessage("Network error or invalid server response format.");
        return fallback;
    }
}

