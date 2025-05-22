# play-java-forms-captcha

This example shows form processing and form helper handling in Play with some popular captcha provider.

## How to run

Start the Play app:
Adapt the `application.conf` file 
- especially the `captcha` settings.
```
captcha {
    type="turnstile" # "recaptcha" or "hcaptcha" or "turnstile"

    #turnstile
    secret="TODO"
    siteKey="TODO"

    #hcaptcha
    #secret="TODO"
    #siteKey="TODO"
}
```
- allowed hosts to test domain configred for captcha

Make sure that you also adjust your hosts file to adapt with domain captcha test registered, because most of captcha doesn't work in localhost:9000 domain.

```
sbt run
```

And open <http://{your-test-domain}:9000/>