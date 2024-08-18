package dvir.meitav.flashlearnai;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIApiService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer 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"
    })
    @POST("v1/chat/completions")
    Call<OpenAIResponse> createCompletion(@Body OpenAIRequest request);
}
