package com.example.mentorisebackend.util;

/**
 * Application-wide constants for error messages, validation messages, and configuration values.
 * All strings used repeatedly across services should be defined here to prevent duplication.
 */
public class AppConstants {

    // ==================== Error Messages ====================

    // User-related errors
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_NOT_FOUND_WITH_ID = "User not found with id: ";
    public static final String UNAUTHENTICATED = "Unauthenticated";

    // Conversation-related errors
    public static final String CONVERSATION_NOT_FOUND = "Conversation not found: ";
    public static final String CONVERSATION_NOT_FOUND_SIMPLE = "Conversation not found";
    public static final String CONVERSATION_READS_ROW_MISSING_RECIPIENT = "conversation_reads row missing for recipient";
    public static final String CONVERSATION_READS_ROW_MISSING_SENDER = "conversation_reads row missing for sender";
    public static final String CONVERSATION_READS_ROW_MISSING = "conversation_reads row missing";
    public static final String CANNOT_CREATE_CONVERSATION_WITH_SELF = "Cannot create conversation with self";
    public static final String USER_NOT_PARTICIPANT_OF_CONVERSATION = "User is not a participant of this conversation";
    public static final String SENDER_NOT_PARTICIPANT_OF_CONVERSATION = "Sender is not a participant of this conversation";

    // Course-related errors
    public static final String COURSE_NOT_FOUND = "Course not found";
    public static final String COURSE_NOT_FOUND_WITH_ID = "Course not found: ";
    public static final String COURSE_CODE_ALREADY_EXISTS = "Course Code already exists";
    public static final String COURSE_CODE_ALREADY_IN_USE = "Course Code Already in use";

    // Major-related errors
    public static final String MAJOR_NOT_FOUND = "Major not found";
    public static final String MAJOR_NOT_FOUND_WITH_ID = "Major not found with id: ";
    public static final String MAJOR_NOT_FOUND_WITH_ID_COLON = "major not found: ";
    public static final String ANOTHER_MAJOR_HAS_SAME_NAME = "Another Major Have The same name";

    // Application-related errors
    public static final String APPLICATION_NOT_FOUND = "Application not found: ";
    public static final String APPLICATION_NOT_FOUND_HEBREW = "לא נמצאה בקשה עם מזהה ";
    public static final String ONLY_PENDING_APPLICATIONS_CAN_BE_PROCESSED = "Only pending applications can be processed";

    // Notification-related errors
    public static final String NOTIFICATION_NOT_FOUND = "Notification not found with id: ";

    // Email/Phone-related errors
    public static final String EMAIL_ALREADY_IN_USE = "Email already in use";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    public static final String EMAIL_ALREADY_IN_USE_ADMIN = "Email Already in use";
    public static final String PHONE_NUMBER_ALREADY_IN_USE = "Phone number already in use";
    public static final String PHONE_NUMBER_ALREADY_EXISTS = "Phone number already exists";
    public static final String NATIONAL_ID_ALREADY_EXISTS = "NationalId already Exists!";
    public static final String NATIONAL_ID_ALREADY_EXISTS_SIMPLE = "ID already exists";

    // ==================== Permission/Authorization Messages ====================

    // Hebrew permission messages
    public static final String ONLY_TUTOR_CAN_SEND_OFFER = "רק מתרגל יכול לשלוח הצעת שיעור";
    public static final String ONLY_STUDENT_CAN_DECLINE = "רק הסטודנט של ההצעה יכול לדחות אותה";
    public static final String NO_PERMISSION_TO_PAY_OFFER = "אין לך הרשאה לשלם על הצעה זו";
    public static final String NO_PERMISSION_TO_CONFIRM_PAYMENT = "אין לך הרשאה לאשר תשלום זה";
    public static final String NOT_ALLOWED_TO_UPDATE_NOTIFICATION = "You are not allowed to update this notification";

    // ==================== Validation Messages ====================

    // Hebrew validation messages
    public static final String START_AND_END_TIME_REQUIRED = "יש להזין זמן התחלה וזמן סיום";
    public static final String STUDENT_USER_ID_REQUIRED = "יש לבחור סטודנט";
    public static final String END_TIME_MUST_BE_AFTER_START = "זמן הסיום חייב להיות אחרי זמן ההתחלה";
    public static final String CANNOT_CREATE_OFFER_FOR_PAST = "לא ניתן ליצור הצעה לזמן שעבר";
    public static final String CANNOT_SEND_OFFER_TO_YOURSELF = "לא ניתן לשלוח הצעה לעצמך";
    public static final String TUTOR_HAS_CONFLICTING_OFFER = "למתרגל כבר קיימת הצעה או פגישה חופפת בזמן הזה";
    public static final String LESSON_DURATION_MUST_BE_45_MINUTES = "משך השיעור חייב להיות בכפולות של 45 דקות";

    // Offer status messages (Hebrew)
    public static final String OFFER_NOT_FOUND = "הצעת השיעור לא נמצאה";
    public static final String OFFER_ALREADY_PAID = "הצעה זו כבר שולמה";
    public static final String OFFER_ALREADY_APPROVED = "הצעת השיעור כבר אושרה";
    public static final String OFFER_CANNOT_BE_PAID_IN_CURRENT_STATUS = "לא ניתן לשלם על הצעה זו במצב הנוכחי";
    public static final String OFFER_NOT_AVAILABLE_FOR_PAYMENT = "ההצעה אינה זמינה לתשלום";
    public static final String OFFER_ALREADY_DECLINED_OR_EXPIRED = "לא ניתן לשלם על הצעה זו במצב הנוכחי";
    public static final String CAN_ONLY_DECLINE_PENDING_OFFER = "ניתן לדחות רק הצעה שממתינה לאישור";
    public static final String OFFER_EXPIRED_CANNOT_DECLINE = "ההצעה פגה ולא ניתן לדחות אותה";

    // Payment messages (Hebrew)
    public static final String PAYMENT_SESSION_NOT_FOUND = "סשן הסליקה לא נמצא";
    public static final String CARD_HOLDER_NAME_REQUIRED = "יש להזין שם בעל כרטיס";
    public static final String INVALID_CARD_NUMBER = "מספר הכרטיס אינו תקין";
    public static final String INVALID_CVV = "CVV אינו תקין";
    public static final String INVALID_CARD_EXPIRY = "תוקף הכרטיס אינו תקין";
    public static final String CARD_DECLINED = "הכרטיס נדחה על ידי ספק הסליקה המדומה";
    public static final String INVALID_CARD_NUMBER_LUHN = "מספר כרטיס לא תקין";
    public static final String PAYMENT_FAILED = "התשלום נכשל";
    public static final String PAYMENT_ALREADY_APPROVED = "התשלום כבר אושר";
    public static final String PAYMENT_CANNOT_BE_CONFIRMED = "לא ניתן לאשר תשלום זה";
    public static final String PAYMENT_CONFIRMED_SUCCESSFULLY = "התשלום אושר בהצלחה";
    public static final String MOCK_PAYMENT_CREATED_SUCCESSFULLY = "נוצרה סליקה מדומה בהצלחה";
    public static final String MOCK_CARD_INSTRUCTIONS = "בסליקה המדומה השתמש בכרטיס בדיקה 4242 4242 4242 4242 להצלחה או 4000 0000 0000 0002 לכישלון";
    public static final String REASON_REQUIRED_FOR_DENIAL = "Reason is required for denial";

    // Profile/Image messages (Hebrew)
    public static final String PROFILE_IMAGE_URL_REQUIRED = "profileImageUrl is required";
    public static final String PROFILE_IMAGE_PUBLIC_ID_REQUIRED = "profileImagePublicId is required";

    // Tutor Application messages (Hebrew)
    public static final String TUTOR_APPLICATION_ALREADY_PENDING = "יש לך כבר בקשת מתרגל שממתינה לבדיקה";
    public static final String REQUEST_EMPTY = "הבקשה ריקה";
    public static final String TRANSCRIPT_URL_REQUIRED = "חובה לצרף קובץ גיליון ציונים (URL מ-Cloudinary)";
    public static final String AT_LEAST_ONE_SCOPE_REQUIRED = "יש לבחור לפחות Scope אחד";
    public static final String CANNOT_MIX_MAJOR_WITH_OTHERS = "לא ניתן לשלב 'כל המסלול' יחד עם קורסים או שנים";

    // Scope/Preferences messages
    public static final String SCOPE_TYPE_REQUIRED = "scopeType is required";
    public static final String USER_MUST_HAVE_MAJOR = "User must have major";
    public static final String YEARS_REQUIRED_FOR_YEAR_SCOPE = "years required for YEAR";
    public static final String COURSE_IDS_REQUIRED_FOR_COURSE_SCOPE = "courseIds required for COURSE";
    public static final String YEAR_OUT_OF_RANGE = "year out of range (1..6): ";
    public static final String COURSE_ID_NOT_FOUND = "courseId not found: ";
    public static final String USER_HAS_MIXED_SCOPE_TYPES = "User has mixed scope types in user_scopes";
    public static final String UNSUPPORTED_SCOPE_TYPE = "Unsupported scopeType: ";
    public static final String SCOPE_TYPE_NOT_PROVIDED = "Field scopeType not provided";
    public static final String MISSING_USER_PARTICIPANT = "משתמש המשויך להצעת השיעור לא נמצא";

    // Chat/Message messages
    public static final String CONVERSATION_ID_REQUIRED = "conversationId is required";
    public static final String SENDER_ID_REQUIRED = "senderId is required";
    public static final String USER_ID_REQUIRED = "userId is required";
    public static final String CLIENT_MESSAGE_ID_REQUIRED = "clientMessageId is required";
    public static final String MESSAGE_TYPE_REQUIRED = "messageType is required";
    public static final String MESSAGE_CONTENT_EMPTY = "Message content is empty";
    public static final String SENDER_NOT_FOUND = "Sender not found: ";

    // Phone number messages
    public static final String INVALID_ISRAELI_PHONE_FORMAT = "Invalid Israeli phone number format: ";

    // ==================== Numeric Constants ====================

    // Duration and pricing
    public static final int LESSON_DURATION_MINUTES = 45;
    public static final int ACADEMIC_HOUR_PRICE = 110;
    public static final int OFFER_VALIDITY_HOURS = 3;

    // Pagination and limits
    public static final int DEFAULT_SWIPE_LIMIT = 10;
    public static final int MAX_SWIPE_LIMIT = 30;
    public static final int MAX_CHAT_LIMIT = 50;
    public static final int MIN_LIMIT = 1;

    // Year range
    public static final int MIN_YEAR = 1;
    public static final int MAX_YEAR = 6;

    // Date filters (days)
    public static final int LAST_7_DAYS = 7;
    public static final int LAST_30_DAYS = 30;
    public static final int LAST_90_DAYS = 90;

    // Card validation
    public static final int MIN_CARD_LENGTH = 13;
    public static final int MAX_CARD_LENGTH = 19;
    public static final int CVV_MIN_LENGTH = 3;
    public static final int CVV_MAX_LENGTH = 4;

    // ==================== WebSocket Queue Paths ====================

    public static final String WEBSOCKET_QUEUE_CHAT = "/queue/chat";
    public static final String WEBSOCKET_QUEUE_NOTIFICATIONS = "/queue/notifications";

    // ==================== Notification Messages (Hebrew) ====================

    public static final String TUTOR_APPLICATION_APPROVED = "הבקשה שלך אושרה";
    public static final String TUTOR_APPLICATION_APPROVED_MESSAGE = "הבקשה שלך להפוך למתרגל אושרה בהצלחה.";
    public static final String TUTOR_APPLICATION_APPROVED_WITH_COMMENT = " הערת אדמין: ";
    public static final String TUTOR_APPLICATION_REJECTED = "הבקשה שלך נדחתה";
    public static final String TUTOR_APPLICATION_REJECTED_MESSAGE = "הבקשה שלך להפוך למתרגל נדחתה. סיבת האדמין: ";
    public static final String USER_PROMOTED_TO_TUTOR = "ברכות! הפכת למתרגל מן המניין";
    public static final String USER_PROMOTED_TO_TUTOR_MESSAGE = "החשבון שלך עודכן בהצלחה וכעת אתה מתרגל מן המניין במערכת, אנא שמור על כללי הקהילה.";

    // ==================== Chat/Session Offer Messages (Hebrew) ====================

    public static final String SESSION_OFFER_SENT = "נשלחה אליך הצעת שיעור";
    public static final String SESSION_OFFER_DATE = "תאריך: ";
    public static final String SESSION_OFFER_TIME = "שעה: ";
    public static final String SESSION_OFFER_SEPARATOR = " - ";
    public static final String SESSION_OFFER_VIEW_DETAILS = "לצפייה בפרטים היכנס/י לשיעורים שלי";

    // ==================== Admin Messages (Hebrew) ====================

    public static final String ADMIN_BROADCAST_PREFIX = "";

    // Tutor role management (admin)
    public static final String CANNOT_DELETE_MAJOR = "לא ניתן למחוק מסלולים דרך מערכת הניהול";
    public static final String CANNOT_DELETE_COURSE = "לא ניתן למחוק קורסים דרך מערכת הניהול";
    public static final String CANNOT_PROMOTE_TO_TUTOR_VIA_ADMIN = "קידום לתפקיד מתרגל מתבצע רק דרך תהליך אישור בקשת מתרגל";
    public static final String USER_IS_NOT_A_TUTOR = "המשתמש אינו מתרגל";
    public static final String TUTOR_NOT_FOUND_OR_INACTIVE = "המתרגל אינו פעיל או לא קיים";

    // ==================== Regex Patterns ====================

    public static final String PHONE_VALIDATION_PATTERN = "^0\\d{9}$";
    public static final String CARD_NUMBER_VALIDATION_PATTERN = "\\d{13,19}";
    public static final String CVV_VALIDATION_PATTERN = "\\d{3,4}";
    public static final String EXPIRY_VALIDATION_PATTERN = "(0[1-9]|1[0-2])/\\d{2}";

    // ==================== Card Numbers (Mock Payment) ====================

    public static final String MOCK_SUCCESS_CARD = "4242424242424242";
    public static final String MOCK_DECLINED_CARD = "4000000000000002";

    // ==================== Card Brands ====================

    public static final String CARD_BRAND_VISA = "VISA";
    public static final String CARD_BRAND_MASTERCARD = "MASTERCARD";
    public static final String CARD_BRAND_AMEX = "AMEX";
    public static final String CARD_BRAND_UNKNOWN = "UNKNOWN";

    // ==================== Prefix Codes ====================

    public static final String CHECKOUT_SESSION_PREFIX = "chk_";
    public static final String TRANSACTION_REF_PREFIX = "txn_";

    // ==================== Locale ====================

    public static final String HEBREW_COUNTRY = "IL";
    public static final String HEBREW_LANGUAGE = "he";

    // ==================== Date Formats ====================

    public static final String DATE_FORMAT_HEBREW = "dd/MM/yyyy";
    public static final String TIME_FORMAT_HEBREW = "HH:mm";

    // ==================== Constraint Names (Database) ====================

    public static final String CONSTRAINT_EMAIL_UNIQUE = "users_email_unique";
    public static final String CONSTRAINT_NATIONAL_ID_UNIQUE = "users_national_id_unique";
    public static final String CONSTRAINT_PHONE_NUMBER_UNIQUE = "users_phone_number_unique";

    private AppConstants() {
        // Private constructor to prevent instantiation
        throw new AssertionError("Cannot instantiate utility class");
    }
}

