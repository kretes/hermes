package pl.allegro.tech.hermes.common.di.factories;

import org.glassfish.hk2.api.Factory;
import pl.allegro.tech.hermes.metrics.PathsCompiler;

import javax.inject.Inject;
import javax.inject.Named;

public class PathsCompilerFactory implements Factory<PathsCompiler> {

    private final String hostname;

    @Inject
    public PathsCompilerFactory(@Named("hostname") String hostname) {
        this.hostname = hostname;
    }

    @Override
    public PathsCompiler provide() {
        return new PathsCompiler(hostname);
    }

    @Override
    public void dispose(PathsCompiler instance) {

    }
}
