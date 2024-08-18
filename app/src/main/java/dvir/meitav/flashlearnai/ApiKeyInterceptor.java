package dvir.meitav.flashlearnai;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class ApiKeyInterceptor implements Interceptor {
    private final String apiKey;

    public ApiKeyInterceptor(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request requestWithApiKey = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + apiKey)
                .build();
        return chain.proceed(requestWithApiKey);
    }
}

