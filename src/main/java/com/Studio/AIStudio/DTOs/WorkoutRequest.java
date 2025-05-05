package com.Studio.AIStudio.DTOs;

import java.util.List;

    public record WorkoutRequest(String workoutType, List<String> muscleGroups, int duration) {
}
