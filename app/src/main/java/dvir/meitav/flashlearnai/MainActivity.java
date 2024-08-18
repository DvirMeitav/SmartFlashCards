package dvir.meitav.flashlearnai;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private OpenAIApiService apiService;
    private CardView cardView;
    private TextView frontTextView;
    private TextView backTextView;
    private List<Flashcard> flashcards;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(OpenAIApiService.class);

        cardView = findViewById(R.id.flashcardView);
        frontTextView = findViewById(R.id.frontTextView);
        backTextView = findViewById(R.id.backTextView);

        Button generateButton = findViewById(R.id.generateButton);
        generateButton.setOnClickListener(v -> {
            Log.d("MainActivity", "Generate button clicked");
            EditText inputEditText = findViewById(R.id.inputEditText);
            String prompt = inputEditText.getText().toString();
            if (!prompt.isEmpty()) {
                Log.d("MainActivity", "Prompt is: " + prompt);
                generateFlashcards(prompt);
            } else {
                Log.d("MainActivity", "Prompt is empty");
            }
        });

        Button flipButton = findViewById(R.id.flipButton);
        Button nextButton = findViewById(R.id.nextButton);

        flipButton.setOnClickListener(v -> flipCard());
        nextButton.setOnClickListener(v -> nextFlashcard());
    }

    private void generateFlashcards(String topic) {
        Log.d("MainActivity", "Starting API call with topic: " + topic);

        String formattedPrompt = "Create a set of flashcards for the topic: " + topic +
                ". Format each flashcard as 'Front: question' and 'Back: answer'.";

        OpenAIRequest.Message userMessage = new OpenAIRequest.Message("user", formattedPrompt);
        List<OpenAIRequest.Message> messages = Collections.singletonList(userMessage);
        OpenAIRequest request = new OpenAIRequest("gpt-3.5-turbo", messages, 150);

        Call<OpenAIResponse> call = apiService.createCompletion(request);

        call.enqueue(new Callback<OpenAIResponse>() {
            @Override
            public void onResponse(Call<OpenAIResponse> call, Response<OpenAIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().getChoices().get(0).getMessage().getContent();
                    Log.d("MainActivity", "API response is successful, result: " + result);
                    displayFlashcards(result);
                } else {
                    try {
                        String errorResponse = response.errorBody().string();
                        Log.d("MainActivity", "API response error: " + errorResponse);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<OpenAIResponse> call, Throwable t) {
                Log.d("MainActivity", "API call failed: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private List<Flashcard> parseFlashcards(String flashcardsText) {
        List<Flashcard> flashcards = new ArrayList<>();
        String[] cards = flashcardsText.split("\n\n");

        for (String card : cards) {
            String[] sides = card.split("\nBack: ");
            if (sides.length == 2) {
                String front = sides[0].replace("Front: ", "").trim();
                String back = sides[1].trim();
                flashcards.add(new Flashcard(front, back));
            }
        }

        return flashcards;
    }

    private void displayFlashcards(String flashcardsText) {
        flashcards = parseFlashcards(flashcardsText);
        currentIndex = 0;

        if (flashcards.isEmpty()) {
            return;
        }

        frontTextView.setText(flashcards.get(currentIndex).getFront());
        backTextView.setVisibility(View.GONE);
    }

    private void flipCard() {
        final ObjectAnimator flipOutAnimator = ObjectAnimator.ofFloat(cardView, "rotationY", 0f, 90f);
        final ObjectAnimator flipInAnimator = ObjectAnimator.ofFloat(cardView, "rotationY", -90f, 0f);

        flipOutAnimator.setDuration(150);
        flipInAnimator.setDuration(150);

        flipOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (backTextView.getVisibility() == View.GONE) {
                    backTextView.setText(flashcards.get(currentIndex).getBack());
                    backTextView.setVisibility(View.VISIBLE);
                    frontTextView.setVisibility(View.GONE);
                } else {
                    backTextView.setVisibility(View.GONE);
                    frontTextView.setVisibility(View.VISIBLE);
                }
                flipInAnimator.start();
            }
        });

        flipOutAnimator.start();
    }

    private void nextFlashcard() {
        currentIndex = (currentIndex + 1) % flashcards.size();
        frontTextView.setText(flashcards.get(currentIndex).getFront());
        backTextView.setVisibility(View.GONE);
    }
}
