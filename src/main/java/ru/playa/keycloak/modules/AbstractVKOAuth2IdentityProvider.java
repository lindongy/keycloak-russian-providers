package ru.playa.keycloak.modules;

import com.fasterxml.jackson.databind.JsonNode;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.io.IOException;
import java.util.List;

/**
 * Базовый провайдер OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 *
 * @param <T> Тип настроек OAuth-авторизации
 * @author Anatoliy Pokhresnyi
 */
public abstract class AbstractVKOAuth2IdentityProvider<T extends AbstractVKIdentityProviderConfig>
extends AbstractRussianOAuth2IdentityProvider<T>
implements SocialIdentityProvider<T> {

    /**
     * Права доступа к данным пользователя по умолчанию.
     */
    private static final String DEFAULT_SCOPE = "";

    /**
     * Список дополнительных настроек провайдера авторизации.
     */
    public static final List<ProviderConfigProperty> CONFIG_PROPERTIES = ProviderConfigurationBuilder
            .create()
            .property()
            .name("version")
            .label("Version VK API")
            .helpText("Version of VK API.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .property()
            .name("emailRequired")
            .label("Email Required")
            .helpText("Is email required (user can be registered in VK via phone)")
            .type(ProviderConfigProperty.BOOLEAN_TYPE)
            .defaultValue("false")
            .add()
            .property()
            .name("fetchedFields")
            .label("Fetched Fields")
            .helpText("Additional fields to need to be fetched")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .build();


    /**
     * Создает объект OAuth-авторизации для российских социальных сейтей.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public AbstractVKOAuth2IdentityProvider(KeycloakSession session, T config) {
        super(session, config);
    }

    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    @Override
    protected String getProfileEndpointForValidation(EventBuilder event) {
        return getConfig().getUserInfoUrl();
    }

    @Override
    protected SimpleHttp buildUserInfoRequest(String subjectToken, String userInfoUrl) {
        return SimpleHttp.doGet(getConfig().getUserInfoUrl() + "&access_token=" + subjectToken, session);
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode node) {
        JsonNode context = JsonUtils.asJsonNode(node, "response");
        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(context, "id"));

        user.setUsername(JsonUtils.asText(context, "screen_name"));
        user.setFirstName(JsonUtils.asText(context, "first_name"));
        user.setLastName(JsonUtils.asText(context, "last_name"));

        user.setIdpConfig(getConfig());
        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, context, getConfig().getAlias());

        return user;
    }

    protected BrokeredIdentityContext extractIdentityFromProfile(JsonNode node, String email) {
        BrokeredIdentityContext user = extractIdentityFromProfile(null, node);

        if (getConfig().isEmailRequired() && StringUtils.isNullOrEmpty(email)) {
            throw new IllegalArgumentException(MessageUtils.email("VK"));
        }

        if (StringUtils.nonNullOrEmpty(email)) {
            user.setUsername(email);
        } else {
            if (StringUtils.isNullOrEmpty(user.getUsername())) {
                user.setUsername("vk." + user.getId());
            }
        }

        user.setEmail(email);

        return user;
    }

    @Override
    public BrokeredIdentityContext getFederatedIdentity(String response) {
        JsonNode node = JsonUtils.asJsonNode(response);
        String accessToken = extractTokenFromResponse(response, getAccessTokenResponseParameter());
        String userId = JsonUtils.asText(node, "user_id");
        String email = JsonUtils.asText(node, "email");

        if (accessToken == null) {
            throw new IdentityBrokerException("No access token available in OAuth server response: " + response);
        }

        BrokeredIdentityContext context = doGetFederatedIdentity(accessToken, userId, email);
        context.getContextData().put(FEDERATED_ACCESS_TOKEN, accessToken);
        return context;
    }

    /**
     * Запрос информации о пользователе.
     *
     * @return Данные авторизованного пользователя.
     */
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken, String userId, String email) {
        try {
            String fields = StringUtils.isNullOrEmpty(getConfig().getFetchedFields())
                    ? "" : "," + getConfig().getFetchedFields();
            String url = getConfig().getUserInfoUrl()
                    + "&access_token=" + accessToken
                    + "&user_ids=" + userId
                    + "&fields=screen_name" + fields
                    + "&name_case=Nom";

            return extractIdentityFromProfile(
                    SimpleHttp
                            .doGet(url, session)
                            .param("content-type", "application/json; charset=utf-8")
                            .asJson(),
                    email);
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from VK: " + e.getMessage(), e);
        }
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {
            String url = getConfig().getUserInfoUrl()
                    + "&access_token=" + accessToken;
            return extractIdentityFromProfile(null, SimpleHttp.doGet(url, session).asJson());
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from VK: " + e.getMessage(), e);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

}
