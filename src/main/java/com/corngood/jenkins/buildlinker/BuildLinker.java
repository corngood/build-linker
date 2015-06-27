package com.corngood.jenkins.buildlinker;

import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;

public class BuildLinker extends Recorder {
    @DataBoundConstructor
    public BuildLinker() {
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public boolean perform(AbstractBuild<?, ?> build,
                           Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        listener.getLogger().println("Tagging build with symlinks");
        build.addAction(new BuildLinkAction());
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Create extended symlinks";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req,formData);
        }
    }

    @Extension
    public static class RunListenerImpl extends RunListener<Run<?,?>> {
        @Override
        public void onDeleted(Run run) {
            BuildLinkAction action = run.getAction(BuildLinkAction.class);
            if (action == null) return;
            System.err.println("Deleted " + action.something);
        }

        @Override
        public void onCompleted(Run<?,?> run, @Nonnull TaskListener listener) {
            BuildLinkAction action = run.getAction(BuildLinkAction.class);
            if (action == null) return;
            System.err.println("Completed " + action.something);
        }
    }
}
