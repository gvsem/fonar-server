package ru.georgii.fonarserver.dialog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class SendMessageDto {

    @NonNull
    @NotEmpty(message = "Message must not be empty.")
    @NotBlank(message = "Message must not be blank.")
    @Schema(example = "plain", description = "Type of message")
    @Pattern(regexp = "plain")
    String type = "";

    @Schema(example = "Hello, world!", description = "Text of message")
    @NotEmpty(message = "Message must not be empty.")
    @NotBlank(message = "Message must not be blank.")
    @Size(min = 1, max = 1000)
    String text;

    public String getType() {return this.type; }
    public String getText() {
        return this.text;
    }

}
