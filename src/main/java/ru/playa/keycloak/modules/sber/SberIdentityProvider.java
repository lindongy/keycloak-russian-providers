package ru.playa.keycloak.modules.sber;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.UriBuilder;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import ru.playa.keycloak.modules.AbstractRussianOAuth2IdentityProvider;
import ru.playa.keycloak.modules.Utils;

import java.io.IOException;
import java.util.UUID;


/**
 * Провайдер OAuth-авторизации через <a href="https://developers.sber.ru/docs/ru/sberid/overview">SberID</a>.
 * <a href="https://developers.sber.ru/docs/ru/sberid/overview">Подробнее</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class SberIdentityProvider
    extends AbstractRussianOAuth2IdentityProvider<SberIdentityProviderConfig>
    implements SocialIdentityProvider<SberIdentityProviderConfig> {

    /**
     * HTTPS.
     */
    private static final String HTTPS = "https://";

    /**
     * Обмен кода подтверждения на токен.
     */
    private static final String TOKEN_URL = "/ru/prod/tokens/v2/oidc";

    /**
     * Запрос информации о пользователе.
     */
    private static final String PROFILE_URL = "/ru/prod/sberbankid/v2.1/userinfo";

    /**
     * Запрос завершения авторизации.
     */
    private static final String COMPLETED_URL = "/api/v2/auth/completed";

    /**
     * Права доступа к данным пользователя по умолчанию.
     */
    private static final String DEFAULT_SCOPE = "";

    /**
     * Создает объект OAuth-авторизации через
     * <a href="https://developers.sber.ru/docs/ru/sberid/overview">SberID</a>.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public SberIdentityProvider(final KeycloakSession session, final SberIdentityProviderConfig config) {
        super(session, config);

        config.setAuthorizationUrl(HTTPS + config.getHost());
        config.setUserInfoUrl(HTTPS + config.getHost() + PROFILE_URL);
        config.setTokenUrl(HTTPS + config.getHost() + TOKEN_URL);
        config.setCompletedUrl(HTTPS + config.getHost() + COMPLETED_URL);
    }

    @Override
    public Object callback(final RealmModel realm, final AuthenticationCallback callback, final EventBuilder event) {
        return new SberEndpoint(callback, event, this, session);
    }

    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    @Override
    protected String getProfileEndpointForValidation(final EventBuilder event) {
        return PROFILE_URL;
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(final EventBuilder event, final JsonNode profile) {
        logger.info("profile: " + profile.toString());

        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(profile, "sub"), getConfig());

        String email = getJsonProperty(profile, "email");
        String username = getJsonProperty(profile, "phone_number");
        if (Utils.nonNullOrEmpty(email)) {
            user.setUsername(email);
        } else {
            if (Utils.nonNullOrEmpty(username)) {
                user.setUsername(username);
            } else {
                user.setUsername("sber." + user.getId());
            }
        }

        user.setEmail(email);
        user.setFirstName(getJsonProperty(profile, "given_name"));
        user.setLastName(getJsonProperty(profile, "family_name"));

        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());

        return user;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(final String accessToken) {
        try {
            final String rquid = UUID.randomUUID().toString().replaceAll("-", "");

            final BrokeredIdentityContext context = extractIdentityFromProfile(
                    null,
                    SimpleHttp
                            .doGet(getConfig().getUserInfoUrl(), session)
                            .header("Authorization", "Bearer " + accessToken)
                            .header("x-ibm-client-id", getConfig().getClientId())
                            .header("x-introspect-rquid", rquid)
                            .asJson()
            );

            final int completed = SimpleHttp
                    .doGet(getConfig().getCompletedUrl(), session)
                    .header("x-introspect-rquid", rquid)
                    .header("Authorization", "Bearer " + accessToken)
                    .asStatus();

            if (completed != 204) {
                logger.warnf("Is not completed. Status %s", completed);
            }

            return context;
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from Sber: " + e.getMessage(), e);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

    @Override
    protected UriBuilder createAuthorizationUrl(final AuthenticationRequest request) {
        return UriBuilder
                .fromUri(getConfig().getAuthorizationUrl())
                .queryParam(OAUTH2_PARAMETER_SCOPE, getConfig().getDefaultScope())
                .queryParam(OAUTH2_PARAMETER_STATE, request.getState().getEncoded())
                .queryParam(OAUTH2_PARAMETER_RESPONSE_TYPE, "code")
                .queryParam(OAUTH2_PARAMETER_CLIENT_ID, getConfig().getClientId())
                .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, request.getRedirectUri())
                .queryParam("display", "page")
                .queryParam("client_type", "PRIVATE");
    }
}