package com.example.testbot.views;

import com.example.testbot.core.SymphonyTest;
import io.dropwizard.views.View;

/**
 * Created by mike.scannell on 11/14/16.
 */
public class SymphonyTestView extends View{
    private final SymphonyTest symphonyTest;

    public enum Template {
        MUSTACHE("mustache/symphonyTest.mustache");

        private String templateName;

        Template(String templateName) {
            this.templateName = templateName;
        }

        public String getTemplateName() {
            return templateName;
        }
    }

    public SymphonyTestView(SymphonyTestView.Template template, SymphonyTest symphonyTest) {
        super(template.getTemplateName());
        this.symphonyTest = symphonyTest;
    }

    public SymphonyTest getSymphonyTest() {
        return symphonyTest;
    }
}
