/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.samples.apps.topeka.widget.quiz;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.samples.apps.topeka.R;
import com.google.samples.apps.topeka.activity.QuizActivity;
import com.google.samples.apps.topeka.model.Category;
import com.google.samples.apps.topeka.model.quiz.Quiz;
import com.google.samples.apps.topeka.widget.fab.DoneFab;
import com.google.samples.apps.topeka.widget.fab.FloatingActionButton;

/**
 * This is the base class for displaying a {@link com.google.samples.apps.topeka.model.quiz.Quiz}.
 * <p>
 * Subclasses need to implement {@link AbsQuizView#createQuizContentView()}
 * in order to allow solution of a quiz.
 * </p>
 * <p>
 * Also {@link AbsQuizView#allowAnswer(boolean)} needs to be called with
 * <code>true</code> in order to mark the quiz solved.
 * </p>
 *
 * @param <Q> The type of {@link com.google.samples.apps.topeka.model.quiz.Quiz} you want to
 * display.
 */
public abstract class AbsQuizView<Q extends Quiz> extends FrameLayout implements
        View.OnClickListener {

    private final Category mCategory;
    private final Q mQuiz;
    private final int mKeylineVertical;
    private final int mKeylineHorizontal;
    private TextView mQuestionView;
    private FloatingActionButton mSubmitAnswer;
    private boolean mAnswered;
    private final LayoutInflater mLayoutInflater;
    protected final int mMinHeightTouchTarget;
    private final Interpolator mInterpolator;

    /**
     * Enables creation of views for quizzes.
     *
     * @param context The context for this view.
     * @param category The {@link Category} this view is running in.
     * @param quiz The actual {@link Quiz} that is going to be displayed.
     */
    public AbsQuizView(Context context, Category category, Q quiz) {
        super(context);
        mQuiz = quiz;
        mCategory = category;
        mKeylineVertical = getResources().getDimensionPixelSize(R.dimen.keyline_16);
        mKeylineHorizontal = getResources()
                .getDimensionPixelSize(R.dimen.keyline_16);
        mSubmitAnswer = getSubmitButton(context);
        mLayoutInflater = LayoutInflater.from(context);
        mMinHeightTouchTarget = getResources()
                .getDimensionPixelSize(R.dimen.min_height_touch_target);
        mInterpolator = AnimationUtils
                .loadInterpolator(getContext(), android.R.interpolator.fast_out_slow_in);
        setId(quiz.getId());
        setUpQuestionView();
        LinearLayout container = createContainerLayout(context);
        View quizContentView = getInitializedContentView();
        addContentView(container, quizContentView);
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                    int oldLeft,
                    int oldTop, int oldRight, int oldBottom) {
                removeOnLayoutChangeListener(this);
                addFloatingActionButton();
            }
        });

    }

    /**
     * Sets the behaviour for all question views.
     */
    private void setUpQuestionView() {
        mQuestionView = (TextView) mLayoutInflater.inflate(R.layout.question, this, false);
        mQuestionView.setText(getQuiz().getQuestion());
    }

    private LinearLayout createContainerLayout(Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setId(R.id.absQuizViewContainer);
        container.setOrientation(LinearLayout.VERTICAL);
        return container;
    }

    private View getInitializedContentView() {
        View quizContentView = createQuizContentView();
        quizContentView.setId(R.id.quiz_content);
        quizContentView.setSaveEnabled(true);
        setDefaultPadding(quizContentView);
        setMinHeightInternal(quizContentView, R.dimen.min_height_question);
        return quizContentView;
    }

    private void addContentView(LinearLayout container, View quizContentView) {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        container.addView(mQuestionView, layoutParams);
        container.addView(quizContentView, layoutParams);
        addView(container, layoutParams);
    }

    private void addFloatingActionButton() {
        final int fabSize = getResources().getDimensionPixelSize(R.dimen.fab_size);
        int bottomOfQuestionView = findViewById(R.id.question_view).getBottom();
        final LayoutParams fabLayoutParams = new LayoutParams(fabSize, fabSize,
                Gravity.END | Gravity.TOP);
        final int fabPadding = getResources().getDimensionPixelSize(R.dimen.padding_fab);
        final int halfAFab = getResources().getDimensionPixelSize(R.dimen.fab_size) / 2;
        fabLayoutParams.setMargins(0, bottomOfQuestionView - halfAFab, 0, fabPadding);
        fabLayoutParams.setMarginEnd(fabPadding);
        addView(mSubmitAnswer, fabLayoutParams);
    }

    private FloatingActionButton getSubmitButton(Context context) {
        if (null == mSubmitAnswer) {
            mSubmitAnswer = new DoneFab(context);
            mSubmitAnswer.setId(R.id.submitAnswer);
            mSubmitAnswer.setVisibility(GONE);
            mSubmitAnswer.setScaleY(0);
            mSubmitAnswer.setScaleX(0);
            //Set QuizActivity to handle clicks on answer submission.
            if (context instanceof QuizActivity) {
                mSubmitAnswer.setOnClickListener(this);
            }
        }
        return mSubmitAnswer;
    }

    private void setDefaultPadding(View view) {
        view.setPadding(mKeylineVertical, mKeylineHorizontal, mKeylineVertical, mKeylineHorizontal);
    }

    protected LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }

    /**
     * Implementations should create the content view for the type of
     * {@link com.google.samples.apps.topeka.model.quiz.Quiz} they want to display.
     *
     * @return the created view to solve the quiz.
     */
    protected abstract View createQuizContentView();

    /**
     * Implementations must make sure that the answer provided is evaluated and correctly rated.
     *
     * @return <code>true</code> if the question has been correctly answered, else
     * <code>false</code>.
     */
    protected abstract boolean isAnswerCorrect();

    /**
     * Save the user input to a bundle for orientation changes.
     *
     * @return The bundle containing the user's input.
     */
    public abstract Bundle getUserInput();

    /**
     * Restore the user's input.
     *
     * @param savedInput The input that the user made in a prior instance of this view.
     */
    public abstract void setUserInput(Bundle savedInput);

    public Q getQuiz() {
        return mQuiz;
    }

    protected boolean isAnswered() {
        return mAnswered;
    }

    /**
     * Sets the quiz to answered or unanswered.
     *
     * @param answered <code>true</code> if an answer was selected, else <code>false</code>.
     */
    protected void allowAnswer(final boolean answered) {
        if (null != mSubmitAnswer) {
            final float targetScale = answered ? 1f : 0f;
            if (answered) {
                mSubmitAnswer.setVisibility(View.VISIBLE);
            }
            mSubmitAnswer.animate().scaleX(targetScale).scaleY(targetScale)
                    .setInterpolator(mInterpolator);
            mAnswered = answered;
        }
    }

    /**
     * Sets the quiz to answered if it not already has been answered.
     * Otherwise does nothing.
     */
    protected void allowAnswer() {
        if (!isAnswered()) {
            allowAnswer(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submitAnswer: {
                submitAnswer(v);
                break;
            }
        }
    }

    /**
     * Allows children to submit an answer via code.
     */
    protected void submitAnswer() {
        submitAnswer(findViewById(R.id.submitAnswer));
    }

    private void submitAnswer(View v) {
        // TODO: 12/15/14 re-architect the way callbacks are being used here.
        if (getContext() instanceof QuizActivity) {
            ((QuizActivity) getContext()).onClick(v);
        }
        mQuiz.setSolved(true);
        mCategory.setScore(getQuiz(), isAnswerCorrect());
    }

    private void setMinHeightInternal(View view, @DimenRes int resId) {
        view.setMinimumHeight(getResources().getDimensionPixelSize(resId));
    }
}
