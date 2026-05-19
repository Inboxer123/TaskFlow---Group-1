package todo.util;

import todo.exception.InvalidTaskException;

public class InputValidator {

    private InputValidator() {}

    public static void requireNonBlank(String value, String fieldName)
            throws InvalidTaskException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidTaskException(fieldName + " cannot be blank.");
        }
    }

    public static void requireMaxLength(String value, int maxLen, String fieldName)
            throws InvalidTaskException {
        if (value != null && value.length() > maxLen) {
            throw new InvalidTaskException(
                    fieldName + " must be " + maxLen + " characters or fewer.");
        }
    }

    public static void requireDateFormat(String value, String fieldName)
            throws InvalidTaskException {
        if (value == null || !value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new InvalidTaskException(
                    fieldName + " must be in YYYY-MM-DD format (e.g. 2025-12-31).");
        }
    }
}