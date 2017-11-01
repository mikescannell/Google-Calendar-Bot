package com.example.testbot;


import com.example.testbot.resources.SymphonyTestResource;
import com.example.testbot.resources.GSuiteBotResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.servlet.FilterRegistration;
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
    public void run(SymphonyTestConfiguration configuration, Environment environment) throws Exception{
        // Enable CORS headers
        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new SymphonyTestResource(configuration));
        environment.jersey().register(new GSuiteBotResource(configuration));
    }
}
