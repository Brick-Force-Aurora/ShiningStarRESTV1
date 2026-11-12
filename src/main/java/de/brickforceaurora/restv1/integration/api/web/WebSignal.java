package de.brickforceaurora.restv1.integration.api.web;

import me.lauriichan.snowframe.signal.ISignal;

public abstract class WebSignal implements ISignal {

    public static final class Setup extends WebSignal {

        private final IAuthenticationProvider original;
        private volatile IAuthenticationProvider authenticationProvider;

        Setup(IAuthenticationProvider authenticationProvider) {
            this.original = authenticationProvider;
            this.authenticationProvider = authenticationProvider;
        }

        public IAuthenticationProvider authenticationProvider() {
            return authenticationProvider;
        }

        public void authenticationProvider(IAuthenticationProvider authenticationProvider) {
            this.authenticationProvider = authenticationProvider == null ? original : authenticationProvider;
        }

    }

    public static final class Start extends WebSignal {

        private final IWebProvider provider;

        Start(IWebProvider provider) {
            this.provider = provider;
        }

        public IWebProvider provider() {
            return provider;
        }

    }

    public static final class Stop extends WebSignal {

        Stop() {}

    }

}
