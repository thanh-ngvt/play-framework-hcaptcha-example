package service;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.time.Duration;

public class TurnstileCaptchaService implements CaptchaService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Config config;
    private final WSClient ws;

    @Inject
    public TurnstileCaptchaService(Config config, WSClient ws) {
        this.config = config;
        this.ws = ws;
    }

    @Override
    public boolean validate(String captchaResponseToken) {
        try {
            String sb = "response=" +
                    captchaResponseToken +
                    "&secret=" +
                    config.getString("captcha.secret");

            WSRequest wsRequest = ws.url("https://challenges.cloudflare.com/turnstile/v0/siteverify")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .setRequestTimeout(Duration.ofSeconds(10));

            WSResponse wsResponse = wsRequest.post(sb).toCompletableFuture().join();

            logger.info("Turnstile response");
            logger.info("response = {}", wsResponse.getBody());
            logger.info("status = {}", wsResponse.getStatus());
            return wsResponse.getBody().contains("\"success\":true"); // Placeholder for actual captcha validation
        } catch (Exception e) {
            logger.info(e.getMessage());
            return false;
        }
    }
}
