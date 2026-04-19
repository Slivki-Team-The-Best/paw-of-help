package ru.urfu.slivky.web.dto;

import jakarta.validation.constraints.Size;

public record TaskApplicationRequest(
        @Size(max = 2000) String message
) {
}
