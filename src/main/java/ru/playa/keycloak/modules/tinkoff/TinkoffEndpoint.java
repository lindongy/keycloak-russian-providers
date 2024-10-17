package ru.playa.keycloak.modules.tinkoff;

import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.Urls;
import ru.playa.keycloak.modules.AbstractRussianEndpoint;

import java.util.Base64;

import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.*;

/**
 * Переопределенный класс {@code AbstractRussianEndpoint}.
 * Класс переопределен с целью изменения логики замены кода на токен.
 */
public class TinkoffEndpoint extends AbstractRussianEndpoint {

    /**
     * Провайдер авторизации.
     */
    private final TinkoffIdentityProvider provider;

    /**
     * Сессия.
     */
    private final KeycloakSession session;

    /**
     * Контекст.
     */
    private final KeycloakContext context;

    /**
     * Конструктор.
     *
     * @param aCallback Callback.
     * @param aEvent    Сервис отправки событий.
     * @param aProvider Провайдер авторизации.
     * @param aSession  Сессия.
     */
    public TinkoffEndpoint(
        final IdentityProvider.AuthenticationCallback aCallback,
        final EventBuilder aEvent,
        final TinkoffIdentityProvider aProvider,
        final KeycloakSession aSession
    ) {
        super(aCallback, aEvent, aProvider, aSession);
        this.provider = aProvider;
        this.session = aSession;
        this.context = aSession.getContext();
    }

    @Override
    public SimpleHttp generateTokenRequest(final String authorizationCode) {
        final String token = provider.getConfig().getClientId() + ":" + provider.getConfig().getClientSecret();

        return SimpleHttp
            .doPost(provider.getConfig().getTokenUrl(), session)
            .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(token.getBytes()))
            .param(OAUTH2_PARAMETER_CODE, authorizationCode)
            .param(
                OAUTH2_PARAMETER_REDIRECT_URI,
                Urls
                    .identityProviderAuthnResponse(
                        context.getUri().getBaseUri(), provider.getConfig().getAlias(), context.getRealm().getName()
                    )
                    .toString()
            )
            .param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
    }
}
