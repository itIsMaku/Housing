package cz.maku.housing.rest;

import cz.maku.mommons.ExceptionResponse;
import cz.maku.mommons.Mommons;
import cz.maku.mommons.Response;
import okhttp3.*;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.File;
import java.io.IOException;

public final class Rests {

    public final static OkHttpClient HTTP_CLIENT = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static Response post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (okhttp3.Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (response.code() > 200) {
                return new ExceptionResponse(Response.Code.ERROR, response.message(), new RuntimeException("Post was not successful: " + response.message()));
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return new ExceptionResponse(Response.Code.ERROR, "Body of response is null.", new RuntimeException("Body of response is null."));
            }
            return Mommons.GSON.fromJson(responseBody.string(), Response.class);
        }
    }

    public static Response upload(String url, File file) throws IOException {
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("text/plain"), file))
                .addFormDataPart("other_field", "other_field_value")
                .build();
        Request request = new Request.Builder().url(url).post(formBody).build();
        try (okhttp3.Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (response.code() > 200) {
                return new ExceptionResponse(Response.Code.ERROR, response.message(), new RuntimeException("File upload was not successful: " + response.message()));
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return new ExceptionResponse(Response.Code.ERROR, "Body of response is null.", new RuntimeException("Body of response is null."));
            }
            return Mommons.GSON.fromJson(responseBody.string(), Response.class);
        }
    }

    public static ResponseValue<File> download(String url, String tempName) throws IOException {
        Request downloadRequest = new Request.Builder().url(url).get().build();

        try (okhttp3.Response response = HTTP_CLIENT.newCall(downloadRequest).execute()) {
            if (!response.isSuccessful() || response.code() > 200) {
                return new ExceptionResponseValue<>(Response.Code.ERROR, response.message(), new RuntimeException("File upload was not successful: " + response.message()), null);
            }

            File file = File.createTempFile(tempName, ".tmp");
            try (BufferedSource bufferedSource = response.body().source()) {
                BufferedSink bufferedSink = Okio.buffer(Okio.sink(file));
                bufferedSink.writeAll(bufferedSource);
                bufferedSink.close();
                return new ResponseValue<>(Response.Code.SUCCESS, null, file);
            }
        }
    }
}
