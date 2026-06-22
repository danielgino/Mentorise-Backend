package com.example.mentorisebackend.dto.tutor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorApplicationCreateRequest {

    private String requestText; // טקסט חופשי מהמשתמש

    @NotBlank(message = "נדרש לצרף קובץ גיליון ציונים (URL מ-Cloudinary)")
    private String transcriptUrl; // הקישור שה-Client שולח אחרי ההעלאה

    @NotEmpty(message = "יש לבחור לפחות תחום אחד (Scope)")
    private List<TutorApplicationScopeCreate> scopes; // קורסים / שנים / כל המסלול
}
