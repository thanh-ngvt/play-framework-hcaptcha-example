package service;

public interface CaptchaService {
    public boolean validate(String captchaResponseToken);

    default String getCaptchaResponseFieldName() {
        if (this instanceof HCaptchaService) {
            return "h-captcha-response";
        } else if (this instanceof TurnstileCaptchaService) {
            return "cf-turnstile-response";
        }
        return "";
    }
}
