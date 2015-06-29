package com.corngood.jenkins.buildlinker;

import hudson.model.Action;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Exported;

@ExportedBean
public class BuildLinkAction implements Action {
    @Exported
    public String[] links = {};

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return "Build Links";
    }

    public String getUrlName() {
        return "build-links";
    }
}
