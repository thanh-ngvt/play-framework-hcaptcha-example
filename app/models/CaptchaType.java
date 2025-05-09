package models;

public enum CaptchaType {
    NONE("none"),
    HCAPTCHA("hcaptcha"),
    RECAPTCHA("recaptcha"),
    TURNSTILE("turnstile");

    private final String value;

    CaptchaType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CaptchaType fromValue(String value) {
        for (CaptchaType type : CaptchaType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown captcha type: " + value);
    }
}
