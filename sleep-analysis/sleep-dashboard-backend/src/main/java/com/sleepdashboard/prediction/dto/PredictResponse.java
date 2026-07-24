package com.sleepdashboard.prediction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictResponse {
    private Double predictedSleepScore;
    private String modelName;
}
