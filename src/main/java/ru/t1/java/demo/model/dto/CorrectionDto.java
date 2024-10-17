package ru.t1.java.demo.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.t1.java.demo.model.Correction;

import java.time.LocalDateTime;

/**
 * DTO for {@link Correction}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class CorrectionDto {

    @JsonProperty("transaction_id")
    long transactionId;

    @JsonProperty("timestamp")
    LocalDateTime timestamp;

    @JsonProperty("retry_count")
    int retryCount;

    @JsonProperty("last_retry_timestamp")
    LocalDateTime lastRetryTimestamp;
}