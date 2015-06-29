package com.corngood.jenkins.buildlinker;

import hudson.Launcher;
import hudson.Extension;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.Job;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.Util;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class BuildLinker extends Recorder {
    private List<Link> links;

    public List<Link> getLinks() {
        return links;
    }

    @DataBoundConstructor
    public BuildLinker (List<Link> links) {
        this.links = links;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
        throws InterruptedException, IOException {
        listener.getLogger().println("Tagging build with symlinks");
        BuildLinkAction action = new BuildLinkAction();
        String[] names = new String[links.size()];
        EnvVars env = build.getEnvironment(listener);
        for (int i = 0; i < names.length; ++i)
            names[i] = env.expand(links.get(i).name);
        action.links = names;
        build.addAction(action);
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
        }

        @Override
        public void onCompleted(Run<?,?> run, @Nonnull TaskListener listener) {
            BuildLinkAction action = run.getAction(BuildLinkAction.class);
            if (action == null) return;
            Job job = run.getParent();
            for (String link: action.links) {
                listener.getLogger().println("creating link " + link);
                Path linkPath = job.getBuildDir().toPath().resolve(link);
                try {
                    Files.createDirectories(linkPath.getParent());
                    Files.deleteIfExists(linkPath);
                    Files.createSymbolicLink(linkPath, linkPath.getParent().relativize(run.getRootDir().toPath()));
                } catch(IOException e) {
                    listener.error("Unable to create symlink", e);
                }
            }
        }
    }

    public static final class Link extends AbstractDescribableImpl<Link> {
        private String name;

        public String getName() {
            return name;
        }

        @DataBoundConstructor
        public Link(String name) {
            this.name = Util.fixEmptyAndTrim(name);
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<Link> {
            @Override
            public String getDisplayName() {
                return "";
            }
        }
    }
}
