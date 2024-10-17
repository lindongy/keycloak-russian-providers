package ru.playa.keycloak.modules.tinkoff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

/**
 * Провайдер OAuth-авторизации через <a href="tinkoff.github.io">Tinkoff</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class TinkoffIdentityProvider
    extends AbstractRussianOAuth2IdentityProvider<TinkoffIdentityProviderConfig>
    implements SocialIdentityProvider<TinkoffIdentityProviderConfig> {

    /**
     * Запрос кода подтверждения.
     */
    private static final String AUTH_URL = "https://id.tinkoff.ru/auth/authorize";

    /**
     * Обмен кода подтверждения на токен.
     */
    private static final String TOKEN_URL = "https://id.tinkoff.ru/auth/token";

    /**
     * Запрос информации о пользователе.
     */
    private static final String PROFILE_URL = "https://id.tbank.ru/userinfo/userinfo";

    /**
     * Права доступа к данным пользователя по умолчанию.
     */
    private static final String DEFAULT_SCOPE = "";

    /**
     * Создает объект OAuth-авторизации через
     * <a href="tinkoff.github.io">Tinkoff</a>.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public TinkoffIdentityProvider(final KeycloakSession session, final TinkoffIdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    @Override
    public Object callback(final RealmModel realm, final AuthenticationCallback callback, final EventBuilder event) {
        return new TinkoffEndpoint(callback, event, this, session);
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
    protected BrokeredIdentityContext extractIdentityFromProfile(final EventBuilder event, final JsonNode node) {
        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(node, "id"), getConfig());

        String email = getJsonProperty(node, "email");
        String login = getJsonProperty(node, "phone_number");
        if (Utils.isNullOrEmpty(login)) {
            user.setUsername(email);
        } else {
            user.setUsername(login);
        }

        user.setEmail(email);
        user.setLastName(getJsonProperty(node, "family_name"));
        user.setFirstName(getJsonProperty(node, "given_name"));

        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, node, getConfig().getAlias());

        return user;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(final String accessToken) {
        try {
            return extractIdentityFromProfile(
                null,
                    SimpleHttp
                        .doPost(getConfig().getUserInfoUrl(), session)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("client_id", getConfig().getClientId())
                        .param("client_secret ", getConfig().getClientSecret())
                        .asJson()
            );
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from Tinkoff: " + e.getMessage(), e);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

    @Override
    protected UriBuilder createAuthorizationUrl(final AuthenticationRequest request) {
        UriBuilder builder = UriBuilder
                .fromUri(getConfig().getAuthorizationUrl())
                .queryParam(OAUTH2_PARAMETER_SCOPE, getConfig().getDefaultScope())
                .queryParam(OAUTH2_PARAMETER_STATE, request.getState().getEncoded())
                .queryParam(OAUTH2_PARAMETER_RESPONSE_TYPE, "code")
                .queryParam(OAUTH2_PARAMETER_CLIENT_ID, getConfig().getClientId())
                .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, request.getRedirectUri());

        if (Utils.nonNullOrEmpty(getConfig().getINN())) {
            builder.queryParam("scope_parameters", getScopeParameters());
        }

        return builder;
    }

    /**
     * Получает данные компании.
     *
     * @return Данные компании.
     */
    private String getScopeParameters() {
        final ObjectNode node = mapper.createObjectNode();

        node.put("inn", getConfig().getINN());

        if (Utils.isNullOrEmpty(getConfig().getKPP())) {
            node.put("kpp", "0");
        } else {
            node.put("kpp", getConfig().getKPP());
        }

        return node.toString();
    }
}