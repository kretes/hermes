package pl.allegro.tech.hermes.common.hostname;

import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;

public class HostnameFactory implements Factory<String> {

    private final String hostname;
    @Inject
    public HostnameFactory(HostnameResolver hostnameResolver) {
        hostname = hostnameResolver.resolve();
    }

    @Override
    public String provide() {
        return hostname;
    }

    @Override
    public void dispose(String instance) {

    }
}
