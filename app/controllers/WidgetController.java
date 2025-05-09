package controllers;

import com.typesafe.config.Config;
import models.CaptchaType;
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
import play.mvc.Http;
import service.CaptchaService;

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
    private final CaptchaType captchaType;
    private final CaptchaService captchaService;

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;

    @Inject
    public WidgetController(FormFactory formFactory, MessagesApi messagesApi, Config config, WSClient ws, CaptchaService captchaService) {
        this.form = formFactory.form(WidgetData.class);
        this.messagesApi = messagesApi;
        this.config = config;
        this.ws = ws;
        this.siteKey = config.getString("captcha.siteKey");
        this.captchaType = CaptchaType.fromValue(config.getString("captcha.type"));
        this.captchaService = captchaService;
        this.widgets = com.google.common.collect.Lists.newArrayList(
                new Widget("Data 1", 123),
                new Widget("Data 2", 456),
                new Widget("Data 3", 789)
        );
    }

    public Result listWidgets(Http.Request request) {
        return ok(views.html.listWidgets.render(asScala(widgets), form, captchaType, siteKey, request, messagesApi.preferred(request)));
    }

    public Result createWidget(Http.Request request) {
        final Form<WidgetData> boundForm = form.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            logger.error("errors = {}", boundForm.errors());
            return badRequest(views.html.listWidgets.render(asScala(widgets), boundForm, captchaType, siteKey, request, messagesApi.preferred(request)));
        } else {
            WidgetData data = boundForm.get();
            String captchaResponse = request.body().asFormUrlEncoded().get(captchaService.getCaptchaResponseFieldName())[0];
            if (captchaResponse != null && !captchaResponse.isEmpty() && captchaService.validate(captchaResponse)) {
                widgets.add(new Widget(data.getName(), data.getPrice()));
                return redirect(routes.WidgetController.listWidgets())
                        .flashing("info", "Widget added!");
            } else {
                logger.error("Captcha validation failed");
                return badRequest(views.html.listWidgets.render(asScala(widgets), boundForm, captchaType, siteKey, request, messagesApi.preferred(request)))
                        .flashing("error", "Captcha validation failed");
            }

        }
    }
}
