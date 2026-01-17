package com.ko.wellness.ui.workouts

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ko.wellness.data.database.AppDatabase
import com.ko.wellness.data.entities.WorkoutExercise
import com.ko.wellness.databinding.ActivityWorkoutPlayerBinding
import kotlinx.coroutines.launch

class WorkoutPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutPlayerBinding
    private lateinit var database: AppDatabase

    private var exercises = listOf<WorkoutExercise>()
    private var currentExerciseIndex = 0
    private var currentSet = 1
    private var isResting = false
    private var isTimerRunning = false

    private var timer: CountDownTimer? = null
    private var remainingTime: Long = 0
    private var totalTime: Long = 0

    private var isProcessing = false

    private val REST_DURATION = 30000L // 30 seconds

    private var completedSetsForCurrentExercise = 0
    private var allSetsCompletedProperly = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        val date = intent.getStringExtra("WORKOUT_DATE") ?: return finish()

        loadWorkout(date)
        setupButtons()
    }

    private fun loadWorkout(date: String) {
        database.workoutExerciseDao()
            .getWorkoutsForDate(date)
            .observe(this) { workoutList ->
                if (workoutList.isNullOrEmpty()) {
                    finish()
                    return@observe
                }

                if (exercises.isEmpty()) {
                    // Sort by Category Order First, then by Order within Category
                    exercises = workoutList.sortedWith(
                        compareBy<WorkoutExercise> { exercise ->
                            when (exercise.category) {
                                "Warm-Up" -> 0
                                "Workout" -> 1
                                "Cooldown" -> 2
                                else -> 3
                            }
                        }.thenBy { it.order }
                    )
                    startExercise()
                }
            }
    }

    private fun setupButtons() {
        binding.buttonClose.setOnClickListener {
            showExitConfirmation()
        }

        binding.buttonPrevious.setOnClickListener {
            previousSet()
        }

        binding.buttonNext.setOnClickListener {
            nextSet()
        }

        binding.buttonPlayPause.setOnClickListener {
            togglePauseResume()
        }

        binding.buttonSkipRest.setOnClickListener {
            skipRest()
        }

        binding.buttonRepsDone.setOnClickListener {
            completeSet()
        }

        // Rep counter +/- buttons
        binding.buttonRepPlus.setOnClickListener {
            val current = binding.editRepCount.text.toString().toIntOrNull() ?: 0
            binding.editRepCount.setText((current + 1).toString())
        }

        binding.buttonRepMinus.setOnClickListener {
            val current = binding.editRepCount.text.toString().toIntOrNull() ?: 0
            if (current > 0) {
                binding.editRepCount.setText((current - 1).toString())
            }
        }
    }

    private fun startExercise() {
        if (currentExerciseIndex >= exercises.size) {
            completeWorkout()
            return
        }

        val exercise = exercises[currentExerciseIndex]
        isResting = false
        isTimerRunning = false

        // Reset Completion Tracking when Starting an Exercise
        if (currentSet == 1) {
            completedSetsForCurrentExercise = 0
            allSetsCompletedProperly = true
        }

        updateUI(exercise)

        if (exercise.duration != null) {
            // Timed exercise
            showTimerDisplay()
            totalTime = exercise.duration * 1000L
            remainingTime = totalTime
            updateTimerDisplay(remainingTime)
            updatePlayPauseButton(true) // Paused initially
        } else {
            // Rep-based exercise
            showRepDisplay(exercise.reps ?: 0)
            binding.editRepCount.setText("0")
        }
    }

    private fun startRest() {
        isResting = true
        isTimerRunning = false

        showRestDisplay()
        totalTime = REST_DURATION
        remainingTime = totalTime
        updateTimerDisplay(remainingTime)
        updatePlayPauseButton(true) // Paused initially
    }

    private fun startTimer(durationMillis: Long) {
        timer?.cancel()

        timer = object : CountDownTimer(durationMillis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                updateTimerDisplay(millisUntilFinished)
            }

            override fun onFinish() {
                isTimerRunning = false
                completeSet()
            }
        }.start()

        isTimerRunning = true
        updatePlayPauseButton(false)
    }

    private fun completeSet() {
        if (isProcessing) return
        isProcessing = true

        // Validate Reps if it's a Rep-Based Exercise
        val exercise = exercises[currentExerciseIndex]
        if (exercise.reps != null) {
            val enteredReps = binding.editRepCount.text.toString().toIntOrNull() ?: 0
            val targetReps = exercise.reps

            if (enteredReps < targetReps) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Incomplete Set")
                    .setMessage("You've only completed $enteredReps of $targetReps reps. Are you sure you want to continue?")
                    .setPositiveButton("Yes, Continue") { _, _ ->
                        allSetsCompletedProperly = false
                        proceedToNextSet()
                    }
                    .setNegativeButton("Go Back") { _, _ ->
                        isProcessing = false
                    }
                    .setOnCancelListener {
                        isProcessing = false
                    }
                    .show()
                return
            } else {
                // Reps Met or Exceeded - this Set was Completed Properly
                completedSetsForCurrentExercise++
            }
        } else {
            // Timed Exercise - Always counts as Completed
            completedSetsForCurrentExercise++
        }

        proceedToNextSet()
    }

    private fun proceedToNextSet() {
        timer?.cancel()
        isTimerRunning = false

        if (currentExerciseIndex >= exercises.size) {
            isProcessing = false
            return
        }

        val exercise = exercises[currentExerciseIndex]

        if (currentSet < exercise.sets) {
            // More Sets Remaining - Start Rest
            currentSet++
            startRest()
        } else {
            // Exercise Complete - Mark as Complete and move to Next Exercise
            if (allSetsCompletedProperly && completedSetsForCurrentExercise == exercise.sets) {
                markExerciseComplete(exercise)
            }

            // Reset Counters for Next Exercise
            completedSetsForCurrentExercise = 0
            allSetsCompletedProperly = true
            currentSet = 1
            currentExerciseIndex++
            startExercise()
        }

        binding.root.postDelayed({ isProcessing = false }, 300)
    }

    private fun markExerciseComplete(exercise: WorkoutExercise) {
        lifecycleScope.launch {
            database.workoutExerciseDao()
                .updateCompletionStatus(exercise.id, true)
        }
    }

    private fun skipRest() {
        timer?.cancel()
        isTimerRunning = false
        startExercise()
    }

    private fun previousSet() {
        if (isProcessing) return
        isProcessing = true

        timer?.cancel()
        isTimerRunning = false

        if (isResting) {
            // Go back to previous set of current exercise
            currentSet--
            if (currentSet < 1) {
                // If We were on set 1's rest, go to previous exercise
                if (currentExerciseIndex > 0) {
                    currentExerciseIndex--
                    val prevExercise = exercises[currentExerciseIndex]
                    currentSet = prevExercise.sets
                    completedSetsForCurrentExercise = 0
                    allSetsCompletedProperly = true
                } else {
                    currentSet = 1
                }
            }
            startExercise()
        } else if (currentSet > 1) {
            // Go to previous set
            if (completedSetsForCurrentExercise > 0) {
                completedSetsForCurrentExercise--
            }
            currentSet--
            startExercise()
        } else if (currentExerciseIndex > 0) {
            // Go to last set of previous exercise
            currentExerciseIndex--
            val prevExercise = exercises[currentExerciseIndex]
            currentSet = prevExercise.sets
            completedSetsForCurrentExercise = 0
            allSetsCompletedProperly = true
            startExercise()
        }

        binding.root.postDelayed({ isProcessing = false }, 300)
    }

    private fun nextSet() {
        if (isProcessing) return
        isProcessing = true

        timer?.cancel()
        isTimerRunning = false

        if (currentExerciseIndex >= exercises.size) {
            isProcessing = false
            return
        }

        val exercise = exercises[currentExerciseIndex]

        if (currentSet < exercise.sets) {
            // Move to next set (with rest)
            currentSet++
            startRest()
        } else {
            // Move to next exercise
            currentSet = 1
            currentExerciseIndex++
            startExercise()
        }

        // Reset Processing Flag after a Short Delay
        binding.root.postDelayed({ isProcessing = false }, 300)
    }

    private fun togglePauseResume() {
        if (isTimerRunning) {
            // Pause
            timer?.cancel()
            isTimerRunning = false
            updatePlayPauseButton(true)
        } else {
            // Resume/Start
            startTimer(remainingTime)
        }
    }

    private fun updateUI(exercise: WorkoutExercise) {
        // Update progress (based on exercises completed, not sets)
        val progress = (currentExerciseIndex * 100) / exercises.size
        binding.progressBar.progress = progress
        binding.textProgress.text = "Exercise ${currentExerciseIndex + 1} of ${exercises.size}"

        // Update exercise info
        binding.textExerciseName.text = exercise.name
        binding.textCategory.text = exercise.category
        binding.textSetInfo.text = "Set $currentSet of ${exercise.sets}"

        // Update navigation buttons
        val hasPrevious = currentExerciseIndex > 0 || currentSet > 1
        binding.buttonPrevious.isEnabled = hasPrevious

        val hasNext = currentExerciseIndex < exercises.size - 1 || currentSet < exercise.sets
        binding.buttonNext.isEnabled = hasNext
    }

    private fun showTimerDisplay() {
        binding.layoutTimer.visibility = View.VISIBLE
        binding.layoutRepCounter.visibility = View.GONE
        binding.layoutRest.visibility = View.GONE
        binding.buttonPlayPause.visibility = View.VISIBLE
    }

    private fun showRepDisplay(targetReps: Int) {
        binding.layoutTimer.visibility = View.GONE
        binding.layoutRepCounter.visibility = View.VISIBLE
        binding.layoutRest.visibility = View.GONE
        binding.buttonPlayPause.visibility = View.GONE

        binding.textRepTarget.text = "Target: $targetReps reps"
    }

    private fun showRestDisplay() {
        binding.layoutTimer.visibility = View.GONE
        binding.layoutRepCounter.visibility = View.GONE
        binding.layoutRest.visibility = View.VISIBLE
        binding.buttonPlayPause.visibility = View.VISIBLE
    }

    private fun updateTimerDisplay(millisUntilFinished: Long) {
        val seconds = (millisUntilFinished / 1000).toInt()
        val minutes = seconds / 60
        val secs = seconds % 60

        val timeString = String.format("%d:%02d", minutes, secs)

        if (isResting) {
            binding.textRestTimer.text = timeString
        } else {
            binding.textTimer.text = timeString
        }
    }

    private fun updatePlayPauseButton(isPaused: Boolean) {
        if (isPaused) {
            binding.buttonPlayPause.setImageResource(android.R.drawable.ic_media_play)
        } else {
            binding.buttonPlayPause.setImageResource(android.R.drawable.ic_media_pause)
        }
    }

    private fun completeWorkout() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Workout Complete!")
            .setMessage("Great job! You've completed your workout.")
            .setPositiveButton("Done") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showExitConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit Workout?")
            .setMessage("Are you sure you want to exit? Your progress will be saved.")
            .setPositiveButton("Exit") { _, _ ->
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onPause() {
        super.onPause()
        if (isTimerRunning) {
            timer?.cancel()
            isTimerRunning = false
            updatePlayPauseButton(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showExitConfirmation()
    }
}