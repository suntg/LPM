package com.example.lpm.v3.config;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class MyEventListener extends EventListener {

    private long bytesTransferred;

    @Override
    public void requestBodyEnd(@NotNull Call call, long byteCount) {

        bytesTransferred += byteCount;
        super.requestBodyEnd(call, byteCount);
    }

    @Override
    public void requestHeadersEnd(@NotNull Call call, @NotNull Request request) {
        bytesTransferred += request.headers().byteCount();
        super.requestHeadersEnd(call, request);
    }

    @Override
    public void responseBodyEnd(@NotNull Call call, long byteCount) {
        bytesTransferred += byteCount;
        super.responseBodyEnd(call, byteCount);
    }

    @Override
    public void responseHeadersEnd(@NotNull Call call, @NotNull Response response) {
        bytesTransferred += response.headers().byteCount();
        super.responseHeadersEnd(call, response);
    }
}
