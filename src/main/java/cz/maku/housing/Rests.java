package cz.maku.housing;

import cz.maku.mommons.ExceptionResponse;
import cz.maku.mommons.Mommons;
import cz.maku.mommons.Response;
import okhttp3.*;

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

}
