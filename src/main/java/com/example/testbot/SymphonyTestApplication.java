package com.example.testbot;


import com.example.testbot.resources.SymphonyTestResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.util.Map;

public class SymphonyTestApplication extends Application<SymphonyTestConfiguration> {
    public static void main(String[] args) throws Exception {
        new SymphonyTestApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<SymphonyTestConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new MigrationsBundle<SymphonyTestConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(SymphonyTestConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(new ViewBundle<SymphonyTestConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(SymphonyTestConfiguration configuration) {
                return configuration.getViewRendererConfiguration();
            }
        });
        bootstrap.addBundle(new ViewBundle());
        //bootstrap.addBundle(new JobsBundle());
    }

    @Override
    public void run(SymphonyTestConfiguration configuration, Environment environment) {
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new SymphonyTestResource(configuration));
    }
}
