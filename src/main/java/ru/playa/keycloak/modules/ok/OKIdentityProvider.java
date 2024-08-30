package ru.playa.keycloak.modules.ok;

import com.fasterxml.jackson.databind.JsonNode;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import ru.playa.keycloak.modules.AbstractRussianOAuth2IdentityProvider;
import ru.playa.keycloak.modules.StringUtils;

import java.io.IOException;


/**
 * Провайдер OAuth-авторизации через <a href="https://ok.ru/">Одноклассники</a>.
 * <a href="https://apiok.ru/ext/oauth/">Подробнее</a>.
 *
 * @author Anatoliy Pokhresnyi
 * @author dmitrymalinin
 */
public class OKIdentityProvider
    extends AbstractRussianOAuth2IdentityProvider<OKIdentityProviderConfig>
    implements SocialIdentityProvider<OKIdentityProviderConfig> {

    /**
     * Запрос кода подтверждения.
     */
    private static final String AUTH_URL = "https://connect.ok.ru/oauth/authorize";

    /**
     * Обмен кода подтверждения на токен.
     */
    private static final String TOKEN_URL = "https://api.ok.ru/oauth/token.do";

    /**
     * Запрос информации о пользователе.
     */
    private static final String PROFILE_URL = "https://api.ok.ru/fb.do";

    /**
     * Права доступа к данным пользователя по умолчанию.
     */
    private static final String DEFAULT_SCOPE = "";

    /**
     * Создает объект OAuth-авторизации через
     * <a href="https://ok.ru/">Одноклассники</a>.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public OKIdentityProvider(KeycloakSession session, OKIdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    @Override
    protected String getProfileEndpointForValidation(EventBuilder event) {
        return PROFILE_URL;
    }

    @Override
    protected SimpleHttp buildUserInfoRequest(String subjectToken, String userInfoUrl) {
        return SimpleHttp.doGet(PROFILE_URL + "?access_token=" + subjectToken, session);
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
        logger.info("profile: " + profile.toString());

        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(profile, "uid"), getConfig());

        String email = getJsonProperty(profile, "email");
        if (getConfig().isEmailRequired() && StringUtils.isNullOrEmpty(email)) {
            throw new IllegalArgumentException(StringUtils.email("OK"));
        }

        String username = getJsonProperty(profile, "login");

        if (StringUtils.nonNullOrEmpty(email)) {
            user.setUsername(email);
        } else {
            if (StringUtils.nonNullOrEmpty(username)) {
                user.setUsername(username);
            } else {
                user.setUsername("ok." + user.getId());
            }
        }

        user.setEmail(email);
        user.setFirstName(getJsonProperty(profile, "first_name"));
        user.setLastName(getJsonProperty(profile, "last_name"));

        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());

        return user;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {


            String params = "application_key="
                + getConfig().getPublicKey()
                + "format=jsonmethod=users.getCurrentUser"
                + StringUtils.hex(StringUtils.md5(accessToken + getConfig().getClientSecret()));

            String url = PROFILE_URL
                + "?application_key=" + getConfig().getPublicKey()
                + "&format=json"
                + "&method=users.getCurrentUser"
                + "&sig=" + StringUtils.hex(StringUtils.md5(params))
                + "&access_token=" + accessToken;

            logger.info("url: " + url);

            return extractIdentityFromProfile(null, SimpleHttp.doGet(url, session).asJson());
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from OK: " + e.getMessage(), e);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }
}