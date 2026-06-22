package com.example.mentorisebackend.dto.auth;

import com.example.mentorisebackend.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDto {

    @NotBlank(message = "תעודת זהות נדרשת")
    @Pattern(regexp = "\\d{5,20}", message = "תעודת זהות חייבת להכיל רק ספרות (5 עד 20)")
    private String nationalId;

    @NotBlank(message = "שם פרטי נדרש")
    @Pattern(
            regexp = "^(?:\\p{InHebrew}{2,}(?:['׳]\\p{InHebrew}+)*)"
                    + "(?: \\p{InHebrew}{2,}(?:['׳]\\p{InHebrew}+)*)*$",
            message = "שם פרטי בעברית (אפשר גרש) עם/בלי רווחים; לפחות 2 אותיות בכל מילה"
    )
    private String firstName;

    @NotBlank(message = "שם משפחה נדרש")
    @Pattern(
            regexp = "^(?:\\p{InHebrew}{2,}(?:['׳]\\p{InHebrew}+)*)"
                    + "(?: \\p{InHebrew}{2,}(?:['׳]\\p{InHebrew}+)*)*$",
            message = "שם משפחה בעברית (אפשר גרש) עם/בלי רווחים; לפחות 2 אותיות בכל מילה"
    )
    private String lastName;

    @NotBlank(message = "כתובת מייל נדרשת")
    @Size(max = 254, message = "מייל ארוך מדי (מקסימום 254 תווים)")
    @Email(message = "כתובת המייל אינה תקינה")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$",
            message = "כתובת המייל אינה תקינה (בדוק תחביר ודומיין)"
    )
    private String email;

    @NotBlank(message = "סיסמה נדרשת")
    @Size(min = 8, max = 32, message = "הסיסמה חייבת להיות בין 8 ל-32 תווים")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=])[A-Za-z\\d@$!%*?&#^()_+\\-=]{8,}$",
            message = "הסיסמה חייבת להכיל לפחות אות גדולה, אות קטנה, מספר ותו מיוחד"
    )
    private String password;
    @NotBlank(message = "חובה מסספר טלפון")
    @Pattern(
            regexp = "^(\\+972|0)([23489]|5[0-9]|7[0-9])[0-9]{7}$",
            message = "מספר טלפון ישראלי אינו תקין"
    )
    private String phoneNumber;

    private boolean isAlumni;

    @NotNull(message = "יש לבחור מגמה/מסלול")
    private Long majorId;

    /** תפקיד ברירת מחדל - סטודנט (לא ניתן לשינוי בהרשמה) */
    private Role role = Role.STUDENT;
}
