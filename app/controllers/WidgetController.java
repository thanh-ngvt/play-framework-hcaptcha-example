package controllers;

import com.typesafe.config.Config;
import models.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import java.time.Duration;
import java.util.List;

import static play.libs.Scala.asScala;

/**
 * An example of form processing.
 *
 * https://playframework.com/documentation/latest/JavaForms
 */
@Singleton
public class WidgetController extends Controller {

    private final Form<WidgetData> form;
    private MessagesApi messagesApi;
    private final List<Widget> widgets;
    private final Config config;
    private final WSClient ws;
    private final String siteKey;

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;

    @Inject
    public WidgetController(FormFactory formFactory, MessagesApi messagesApi, Config config, WSClient ws) {
        this.form = formFactory.form(WidgetData.class);
        this.messagesApi = messagesApi;
        this.config = config;
        this.ws = ws;
        this.siteKey = config.getString("captcha.siteKey");
        this.widgets = com.google.common.collect.Lists.newArrayList(
                new Widget("Data 1", 123),
                new Widget("Data 2", 456),
                new Widget("Data 3", 789)
        );
    }

    public Result index() {
        return ok(views.html.index.render());
    }

    public Result listWidgets(Http.Request request) {
        return ok(views.html.listWidgets.render(asScala(widgets), form, siteKey, request, messagesApi.preferred(request)));
    }

    public Result createWidget(Http.Request request) {
        final Form<WidgetData> boundForm = form.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            logger.error("errors = {}", boundForm.errors());
            return badRequest(views.html.listWidgets.render(asScala(widgets), boundForm, siteKey, request, messagesApi.preferred(request)));
        } else {
            WidgetData data = boundForm.get();
            String captchaResponse = request.body().asFormUrlEncoded().get("h-captcha-response")[0];
            if (captchaResponse != null && !captchaResponse.isEmpty() && validateCaptcha(captchaResponse)) {
                widgets.add(new Widget(data.getName(), data.getPrice()));
                return redirect(routes.WidgetController.listWidgets())
                        .flashing("info", "Widget added!");
            } else {
                logger.error("Captcha validation failed");
                return badRequest(views.html.listWidgets.render(asScala(widgets), boundForm, siteKey, request, messagesApi.preferred(request)))
                        .flashing("error", "Captcha validation failed");
            }

        }
    }

    public boolean validateCaptcha(String captchaResponse) {
        try {
            String sb = "response=" +
                    captchaResponse +
                    "&secret=" +
                    config.getString("captcha.secret");

            WSRequest wsRequest = ws.url("https://hcaptcha.com/siteverify")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .setRequestTimeout(Duration.ofSeconds(10));

            WSResponse wsResponse = wsRequest.post(sb).toCompletableFuture().join();

            logger.info("hCAPTCHA response");
            logger.info("response = {}", wsResponse.getBody());
            logger.info("status = {}", wsResponse.getStatus());
            return wsResponse.getBody().contains("\"success\":true"); // Placeholder for actual captcha validation
        } catch (Exception e) {
            logger.info(e.getMessage());
            return false;
        }
    }
}
