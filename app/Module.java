import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import models.CaptchaType;
import play.Environment;
import service.HCaptchaService;
import service.CaptchaService;
import service.TurnstileCaptchaService;

public class Module extends AbstractModule {

    private final Environment env;
    private final Config config;

    public Module(Environment env, Config config) {
        this.env = env;
        this.config = config;
    }

    @Override
    protected void configure() {
        String captchaType = config.getString("captcha.type");
        CaptchaType type = CaptchaType.fromValue(captchaType);

        switch (type) {
            case HCAPTCHA:
                bind(CaptchaService.class).to(HCaptchaService.class);
                break;
//            case RECAPTCHA:
//                bind(ICaptchaService.class).to(ReCaptchaService.class);
//                break;
            case TURNSTILE:
                bind(CaptchaService.class).to(TurnstileCaptchaService.class);
                break;
        }
    }
}
