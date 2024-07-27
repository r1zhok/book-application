package book.catalogue.controller.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NewBookPayload(

        @NotNull(message = "{catalog.errors.null}")
        @NotBlank(message = "{catalog.errors.blank}")
        @Size(min = 3, max = 50, message = "{catalog.errors.size}")
        String name,
        @NotNull(message = "{catalog.errors.null}")
        @NotBlank(message = "{catalog.errors.blank}")
        @Size(min = 3, max = 50, message = "{catalog.errors.size}")
        String author,
        @NotBlank(message = "{catalog.errors.blank}")
        @Size(max = 1000, message = "{catalog.errors.size1}")
        String details
) {
}
